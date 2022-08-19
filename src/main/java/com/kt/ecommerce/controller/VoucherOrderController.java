package com.kt.ecommerce.controller;

import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Resource
    private IVoucherOrderService service;

    @PostMapping("seckill/{id}")
    public KTHttpResponse secKillVoucher(@PathVariable("id") Long voucherId) {
        return service.seckillVoucher(voucherId);
    }
}
