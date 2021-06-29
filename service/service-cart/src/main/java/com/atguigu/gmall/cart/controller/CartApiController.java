package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author cyl
 * @Date 2021/6/22 19:43
 * @Version 1.0
 */
@Api(tags = "购物车操作")
@RequestMapping("api/cart")
@RestController
public class CartApiController {

    @Autowired
    private CartService cartService;

//    http://cart.gmall.com/addCart.html?skuId=47&skuNum=1&sourceType=query
    @ApiOperation("添加购物车")
    @RequestMapping("addToCart/{skuId}/{skuNum}")
    public Result addCart(@PathVariable String skuId, @PathVariable String skuNum, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addCart(skuId, userId, skuNum);
        return Result.ok();
    }

    @ApiOperation("查询购物车")
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfos = cartService.cartList(userId, userTempId);
        return Result.ok(cartInfos);
    }

    @ApiOperation("购物车选择状态更新")
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId, @PathVariable Integer isChecked, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.checkCart(skuId, isChecked, userId);
        return Result.ok();
    }

    @ApiOperation("删除购物车")
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

    @ApiOperation("根据用户id查询购物车列表")
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable Long userId) {
        return cartService.getCartCheckedList(userId);
    }

    @GetMapping("loadCartCache/{userId}")
    public Result loadCartCache(@PathVariable("userId") String userId) {
        cartService.loadCartCache(userId);
        return Result.ok();
    }


}
