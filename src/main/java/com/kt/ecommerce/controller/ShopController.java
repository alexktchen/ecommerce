package com.kt.ecommerce.controller;


import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.entity.Shop;
import com.kt.ecommerce.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    @GetMapping("/{id}")
    public KTHttpResponse queryShopById(@PathVariable("id") Long id) throws IOException {
        return shopService.queryById(id);
    }

    @PutMapping
    public KTHttpResponse updateShop(@RequestBody Shop shop) {

        return shopService.update(shop);
    }
}
