package com.kt.ecommerce.utils;

public interface ILock {
    boolean tryLock(long timeoutSec);
    void unlock();
}
