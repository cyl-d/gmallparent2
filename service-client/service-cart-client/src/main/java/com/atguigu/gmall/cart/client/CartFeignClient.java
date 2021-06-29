package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author cyl
 * @Date 2021/6/23 8:25
 * @Version 1.0
 */

@FeignClient(value = "service-cart", fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    @ApiOperation("添加购物车")
    @RequestMapping("api/cart/addToCart/{skuId}/{skuNum}")
    Result addCart(@PathVariable String skuId, @PathVariable String skuNum);


    @ApiOperation("查询购物车")
    @GetMapping("api/cart/cartList")
    Result cartList(HttpServletRequest request);

    @ApiOperation("购物车选择状态更新")
    @GetMapping("api/cart/checkCart/{skuId}/{isChecked}")
    Result checkCart(@PathVariable Long skuId, @PathVariable Integer isChecked);

    @ApiOperation("删除购物车")
    @DeleteMapping("api/cart/deleteCart/{skuId}")
    Result deleteCart(@PathVariable Long skuId, HttpServletRequest request);

    @ApiOperation("根据用户id查询购物车列表")
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable Long userId);

    @ApiOperation("重新加载购物车数据")
    @GetMapping("/api/cart/loadCartCache/{userId}")
    Result loadCartCache(@PathVariable("userId") String userId);


}
