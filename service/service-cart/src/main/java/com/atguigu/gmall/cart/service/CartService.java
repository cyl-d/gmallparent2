package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @Author cyl
 * @Date 2021/6/22 20:10
 * @Version 1.0
 */
public interface CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addCart(String skuId, String userId, String skuNum);

    /**
     * 获取购物车集合
     * @param userId
     * @param userTempId
     * @return
     */
    List<CartInfo> cartList(String userId, String userTempId);

    /**
     * 购物车选中状态变更
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(Long skuId, Integer isChecked, String userId);

    /**
     * 删除购物车
     * @param skuId
     * @param userId
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 获取当前用户id 下所选中的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(Long userId);


    /**
     * 重新加载购物车数据
     * @param userId
     */
    List<CartInfo> loadCartCache(String userId);
}
