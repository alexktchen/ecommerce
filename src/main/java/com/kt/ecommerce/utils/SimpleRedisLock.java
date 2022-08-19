package com.kt.ecommerce.utils;

import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;
    private static final String key_prefix = "lock:";
    private static final String id_prefix = UUID.randomUUID().toString().replace("-", "") + "-";

    private static final DefaultRedisScript<Long> unlock_script;


    static {
        unlock_script = new DefaultRedisScript<>();
        unlock_script.setLocation(new ClassPathResource("unlock.lua"));
        unlock_script.setResultType(Long.class);
    }

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String threadId = id_prefix + Thread.currentThread().getId();
        // 1. Get the lock
        Boolean success = stringRedisTemplate.opsForValue()
            .setIfAbsent(key_prefix + this.name, threadId, timeoutSec, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {

        String threadId = id_prefix + Thread.currentThread().getId();

//        String id = stringRedisTemplate.opsForValue().get(key_prefix + this.name);
//
//        if (threadId.equals(id)) {
//            stringRedisTemplate.delete(key_prefix + this.name);
//        }
        stringRedisTemplate.execute(unlock_script, Collections.singletonList(key_prefix + this.name), threadId);

    }
}
