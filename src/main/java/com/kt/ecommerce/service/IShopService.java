package com.kt.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.Shop;

import java.io.IOException;

public interface IShopService extends IService<Shop> {
    KTHttpResponse queryById(Long id) throws IOException;

    KTHttpResponse update(Shop shop);
}
