package com.kt.ecommerce.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.SeckillVoucher;
import com.kt.ecommerce.entity.VoucherOrder;
import com.kt.ecommerce.mapper.VoucherOrderMapper;
import com.kt.ecommerce.service.ISeckillVoucherService;
import com.kt.ecommerce.service.IVoucherOrderService;
import com.kt.ecommerce.utils.RedisIdWorker;
import com.kt.ecommerce.utils.SimpleRedisLock;
import com.kt.ecommerce.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public KTHttpResponse seckillVoucher(Long voucherId) {
        // 1. query
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2. check the kill is start
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return KTHttpResponse.fail("the sec kill not yet start");
        }
        // 3. check the kill is end
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return KTHttpResponse.fail("the sec kill is end");
        }
        // 4. checkout stock
        if (voucher.getStock() < 1) {
            return KTHttpResponse.fail("the stock error");
        }

        Long userId = UserHolder.getUser().getId();

//        synchronized (userId.toString().intern()) {
//            // The proxy object (Transactional)
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId, voucher);
//        }

        //SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, this.stringRedisTemplate);

        RLock lock = redissonClient.getLock("lock:order:" + userId);


        boolean isLock = lock.tryLock();
        if (!isLock) {
            return KTHttpResponse.fail("can't reorder");
        }

        try {
            // The proxy object (Transactional)
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId, voucher);
        } finally {
            lock.unlock();
        }

    }

    @Transactional
    public KTHttpResponse createVoucherOrder(Long voucherId, SeckillVoucher voucher) {

        Long userId = UserHolder.getUser().getId();

        // 5. one user one order
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return KTHttpResponse.fail("this user has been order");
        }

        // 6. minus stock
        boolean success = seckillVoucherService.update()
            .setSql("stock = stock - 1")
            .eq("voucher_id", voucher.getVoucherId()).gt("stock", 0)
            .update();

        if (!success) {
            return KTHttpResponse.fail("stock error");
        }


        // 6. create order
        long orderId = redisIdWorker.nextId("order");


        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setCreateTime(voucher.getCreateTime());
        voucherOrder.setUpdateTime(voucher.getUpdateTime());
        save(voucherOrder);
        return KTHttpResponse.ok(orderId);


    }
}
