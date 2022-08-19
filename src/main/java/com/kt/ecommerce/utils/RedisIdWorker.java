package com.kt.ecommerce.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200L;
    private static final long COUNT_BITS = 32;
    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 1. 生成時間戳
        LocalDateTime now = LocalDateTime.now();
        long nowSec = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSec - BEGIN_TIMESTAMP;

        // 2. 生成序列號
        // 2.1.
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = this.stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 3. 拼接
        return timestamp << COUNT_BITS | count;
    }
}
