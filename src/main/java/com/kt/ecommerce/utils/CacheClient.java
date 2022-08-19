package com.kt.ecommerce.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(key, new ObjectMapper().writeValueAsString(value), time, unit);
    }

    public <T> void setWithLogicalExpire(String key, T value, Long time, TimeUnit unit) throws JsonProcessingException {
        RedisData<T> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, new ObjectMapper().writeValueAsString(redisData), time, unit);
    }

    public <T, E> T queryWithPassThrough(String keyPrefix, E id, Class<T> type, long time, TimeUnit unit, Function<E, T> dbFallback) throws JsonProcessingException {

        String key = keyPrefix + id;

        // 1. get item from redis
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. is existing
        if (json != null) {
            // redis existing, but dummy empty item
            if (!json.isBlank()) {
                // 3. redis exist. return
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(json, type);
            }
            return null;
        }


        // 4. not existing, query database
        T t = dbFallback.apply(id);

        // 5. id not existing return error
        if (t == null) {
            // set the null value
            // 解決 redis 穿透問題，用一個假的空字串 dummy item
            // 也可以用布林過濾器來做
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 6. database existing, write into redis
        this.set(key, t, time, unit);
        return t;
    }


    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <T, E> T queryWithWithLogicalExpire(String keyPrefix, E id, Class<T> type, long time, TimeUnit unit, Function<E, T> dbFallback) throws IOException {
        String key = keyPrefix + id;

        // 1. get item from redis
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. is not existing
        if (json == null || json.isBlank()) {
            // 3. redis not exist. return null
            return null;
        }


        // 4. existing
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(RedisData.class, type);
        RedisData<T> redisData = objectMapper.readValue(json, javaType);
        T t = redisData.getData();
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5. check is expire
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 not expire
            return t;
        }


        // 6. rebuild cache
        // 6.1 get mutex
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);

        // 6.2 check it can get mutex
        if (isLock) {
            // 6.3 successful get mutex, create thread
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T item = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, item, time, unit);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }

        return t;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
