package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author atguigu-mqx
 */
@Service
public class ItemServiceImpl implements ItemService {

    //  做数据汇总：service-product 这个微服务！
    //  需要使用远程调用：
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map<String, Object> map = new HashMap<>();
        /*
        1.	查询sku的基本信息
        2.	查询skuImage
        3.	分类信息
        4.	spu销售属性，属性值
        5.	查询最新价格
        6.  实现切换功能，需要后台获取到Json 字符串！
         */
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null){
            //  分类信息
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

            //  spu销售属性，属性值
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());

            //  查询最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);

            // 实现切换功能，需要后台获取到Json 字符串！
            Map maps = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  将maps 转换为json 数据 {"115|117":"44","114|117":"45"}
            String mapJson = JSON.toJSONString(maps);

            //  需要将数据存储到map 中！ key = 是谁 ?页面中${}中的内容！  value = 数据值 | map.put(key,value) Thymeleaf ${key}
            map.put("skuInfo",skuInfo);
            map.put("categoryView",categoryView);
            map.put("spuSaleAttrList",spuSaleAttrList);
            map.put("price",skuPrice);
            map.put("valuesSkuJson",mapJson);

        }
        //  返回数据
        return map;
    }
}
