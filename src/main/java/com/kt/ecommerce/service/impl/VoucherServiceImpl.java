package com.kt.ecommerce.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.SeckillVoucher;
import com.kt.ecommerce.entity.Voucher;
import com.kt.ecommerce.mapper.VoucherMapper;
import com.kt.ecommerce.service.ISeckillVoucherService;
import com.kt.ecommerce.service.IVoucherService;
import com.kt.ecommerce.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    ISeckillVoucherService seckillVoucherService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public KTHttpResponse queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        return KTHttpResponse.ok(vouchers);
    }


    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        save(voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucher.setCreateTime(voucher.getCreateTime());
        seckillVoucher.setUpdateTime(voucher.getUpdateTime());


        seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());

    }
}
