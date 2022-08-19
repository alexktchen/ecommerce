package com.kt.ecommerce.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.Shop;
import com.kt.ecommerce.mapper.ShopMapper;
import com.kt.ecommerce.service.IShopService;
import com.kt.ecommerce.utils.CacheClient;
import com.kt.ecommerce.utils.RedisConstants;
import com.kt.ecommerce.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.kt.ecommerce.utils.RedisConstants.*;


@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;
    @Override
    public KTHttpResponse queryById(Long id) throws IOException {

        // 緩存雪崩Cache Avalanche

        // 緩存穿透 Cache Penetration
        // Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, CACHE_SHOP_TTL, TimeUnit.MINUTES, this::getById);

        // 緩存擊穿 Hotspot Invalid, Mutex
        Shop shop = queryWithMutex(id);

        // 緩存擊穿Hotspot Invalid, Mutex
        //Shop shop = cacheClient.queryWithWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, 10L, TimeUnit.SECONDS, this::getById);

        if (shop == null) {
            return KTHttpResponse.fail("shop not existing");
        }

        return KTHttpResponse.ok(shop);
    }

    @Override
    @Transactional
    public KTHttpResponse update(Shop shop) {

        Long id = shop.getId();

        if (id == null) {
            return KTHttpResponse.fail("Shop id can not be null");
        }
        // 1. update database
        updateById(shop);
        // 2. delete cache
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return KTHttpResponse.ok();
    }

    public void saveShop2Redis(Long id, long expireSeconds) throws JsonProcessingException, InterruptedException {
        // 1. query shop
        Shop shop = getById(id);
        Thread.sleep(200);
        // 2. package
        RedisData<Shop> redisData = new RedisData<>();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // write to Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, new ObjectMapper().writeValueAsString(redisData));
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }
    public Shop queryWithMutex(Long id) throws JsonProcessingException {
        String key = CACHE_SHOP_KEY + id;

        // 1. get item from redis
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // 2. is existing
        if (shopJson != null) {
            // redis existing, but dummy empty item
            if (shopJson.isBlank()) {
                return null;
            }
            // 3. redis exist. return
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(shopJson, Shop.class);
        }

        // implement rebuild redis
        // 4.1 get mutex
        String lockKey = "lock:shop:" + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 check to get mutex
            if (!isLock) {
                // 4.3 failed, sleep
                Thread.sleep(50);

                // retry
                return queryWithMutex(id);
            }

            // 4.4. not existing, query database
            shop = getById(id);
            Thread.sleep(200);

            // 5. id not existing return error
            if (shop == null) {
                // set the null value
                // 解決 redis 穿透問題，用一個假的空字串 dummy item
                // 也可以用布林過濾器來做
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }

            String str = new ObjectMapper().writeValueAsString(shop);
            // 6. database existing, write into redis
            stringRedisTemplate.opsForValue().set(key, str, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. release mutex
            unlock(lockKey);
        }


        // 8. return
        return shop;
    }
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}
