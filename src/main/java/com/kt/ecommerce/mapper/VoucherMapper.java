package com.kt.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kt.ecommerce.entity.Voucher;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}