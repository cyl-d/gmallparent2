package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author cyl
 * @Date 2021/6/22 20:11
 * @Version 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeignClient productFeignClient;
    /**
     * 添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(String skuId, String userId, String skuNum) {
//        添加购物车
        String cartKey = getCartKey(userId);
        if (!redisTemplate.hasKey(cartKey)) {
//           加载数据库 存入redis 中
            loadCartCache(userId);
        }
//        查询缓存中是否有该商品
        CartInfo cartInfo = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId);
//        缓存中有商品
        if (cartInfo != null) {
            cartInfo.setSkuNum(cartInfo.getSkuNum() + Integer.parseInt(skuNum));
            cartInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            cartInfo.setIsChecked(1);
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(Long.parseLong(skuId)));
            cartInfoMapper.updateById(cartInfo);

        } else {
//            缓存中无商品
            cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
            cartInfo.setCartPrice(productFeignClient.getSkuPrice(Long.parseLong(skuId)));
            cartInfo.setIsChecked(1);
            cartInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            cartInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            cartInfo.setSkuId(Long.parseLong(skuId));
            cartInfo.setSkuNum(Integer.parseInt(skuNum));
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insert(cartInfo);
        }
        redisTemplate.boundHashOps(cartKey).put(skuId, cartInfo);
//        设置过期时间
        setCartKeyExpire(cartKey);
    }

    /**
     * 加载数据库 存入redis 中
     * @param userId
     */
    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfos = cartInfoMapper.selectList(
                new QueryWrapper<CartInfo>().eq("user_id", userId).orderByDesc("update_time"));
        if (CollectionUtils.isEmpty(cartInfos)) {
            return cartInfos;
        }
        String cartKey = getCartKey(userId);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        cartInfos.forEach((cartInfo -> {
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            hashMap.put(cartInfo.getSkuId().toString(), cartInfo);
        }));
        redisTemplate.opsForHash().putAll(cartKey, hashMap);
        setCartKeyExpire(cartKey);
        return cartInfos;

    }

    /**
     * 设置过期时间
     * @param cartKey
     */
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 通过用户id 获取cart 的key
     * @param userId
     * @return
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    /**
     * 删除购物车
     * @param skuId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(Long skuId, String userId) {
//        删除数据库中对应数据
        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));
//        删缓存对应数据
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        if(boundHashOperations.hasKey(skuId.toString())) {
            boundHashOperations.delete(skuId.toString());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkCart(Long skuId, Integer isChecked, String userId) {
//       修改数据库
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.update(cartInfo, new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));
//        修改缓存
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        Boolean aBoolean = boundHashOperations.hasKey(skuId.toString());
        if (aBoolean) {
            CartInfo cartInfo1 = boundHashOperations.get(skuId.toString());
            cartInfo1.setIsChecked(isChecked);
            boundHashOperations.put(skuId.toString(), cartInfo1);
            setCartKeyExpire(cartKey);
        }

    }

    /**
     * 获取购物车集合
     * @param userId
     * @param userTempId
     * @return
     */
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {

        List<CartInfo> cartInfos = new ArrayList<>();
//        判断是否登录
        if (StringUtils.isEmpty(userId)) {
            return this.cartList(userTempId);
        }
//        判断是否需要进行合并
        if (!StringUtils.isEmpty(userTempId) && !CollectionUtils.isEmpty(this.cartList(userTempId))) {
//                合并数据
           cartInfos = this.mergeToCartList(this.cartList(userTempId), userId);
//               删除 临时数据
           this.deleteCartList(userTempId);
        } else {
            cartInfos = this.cartList(userId);
        }
        return cartInfos;
    }

    /**
     * 对对应UserId进行一个内部购物车的删除
     * @param userId
     */
    private void deleteCartList(String userId) {
//        删除数据库的数据
        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userId));
//        删除redis 数据
        String cartKey = getCartKey(userId);
        Boolean aBoolean = redisTemplate.hasKey(cartKey);
        if (aBoolean) {
            redisTemplate.delete(cartKey);
        }
    }

    /**
     * 进行一个分支的合并
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
//        获取用户id的集合
        List<CartInfo> cartInfos = cartList(userId);
        Map<Long, CartInfo> cartInfoMap = cartInfos.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        cartInfoNoLoginList.forEach((cartInfo -> {
            CartInfo info = cartInfoMap.get(cartInfo.getSkuId());
//            判断当前信息是否在登录用户购物车存在
            if (info != null) {
//                存在
//                设置数量
                info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
//                设置修改时间
                info.setUpdateTime(new Timestamp(System.currentTimeMillis()));
//                设置选中状态
                if (info.getIsChecked() == 1 || cartInfo.getIsChecked() == 1) {
                    info.setIsChecked(1);
                }
                cartInfoMapper.updateById(info);
            } else {
//                不存在
                cartInfo.setUserId(userId);
                cartInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                cartInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
                cartInfoMapper.insert(cartInfo);
            }
        }));
//        取出数据 放入缓存
        List<CartInfo> cartInfoList = this.loadCartCache(userId);
        return cartInfoList;
    }

    /**
     * 根据id 获取用户集合
     * @param userId
     * @return
     */
    private List<CartInfo> cartList(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        if (StringUtils.isEmpty(userId)) {
            return cartInfos;
        }
        String cartKey = getCartKey(userId);
        cartInfos = redisTemplate.opsForHash().values(cartKey);
        if (CollectionUtils.isEmpty(cartInfos)) {
//            redis 中无数据
            cartInfos = loadCartCache(userId);
        } else {
//            redis 中有数据
//          排序
            cartInfos.sort((o1, o2) ->
                DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND)
            );

        }
        return cartInfos;
    }


}
