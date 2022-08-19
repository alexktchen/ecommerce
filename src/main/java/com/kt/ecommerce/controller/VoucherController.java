package com.kt.ecommerce.controller;

import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.Voucher;
import com.kt.ecommerce.service.IVoucherService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher")
public class VoucherController {
    @Resource
    private IVoucherService voucherService;

    @PostMapping("seckill")
    public KTHttpResponse addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return KTHttpResponse.ok(voucher.getId());
    }
}
