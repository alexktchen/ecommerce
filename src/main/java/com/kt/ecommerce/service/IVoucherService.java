package com.kt.ecommerce.service;

import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.Voucher;

public interface IVoucherService {
    KTHttpResponse queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
