package com.atguigu.gmall.cart.client.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author cyl
 * @Date 2021/6/23 8:30
 * @Version 1.0
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {
    @Override
    public Result addCart(String skuId, String skuNum) {
        return null;
    }

    @Override
    public Result cartList(HttpServletRequest request) {
        return null;
    }

    @Override
    public Result checkCart(Long skuId, Integer isChecked) {
        return null;
    }

    @Override
    public Result deleteCart(Long skuId, HttpServletRequest request) {
        return null;
    }
}
