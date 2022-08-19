package com.kt.ecommerce.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final long LOGIN_CODE_TTL = 2L;

    public static final String LOGIN_USER_KEY = "login:token:";
    public static final long LOGIN_USER_TTL = 360000L;

    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final long CACHE_SHOP_TTL = 30L;

    public static final long CACHE_NULL_TTL = 2L;

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

}
