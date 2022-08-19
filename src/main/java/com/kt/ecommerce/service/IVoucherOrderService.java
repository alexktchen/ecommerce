package com.kt.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.SeckillVoucher;
import com.kt.ecommerce.entity.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    KTHttpResponse seckillVoucher(Long voucherId);

    KTHttpResponse createVoucherOrder(Long voucherId, SeckillVoucher voucher);
}
