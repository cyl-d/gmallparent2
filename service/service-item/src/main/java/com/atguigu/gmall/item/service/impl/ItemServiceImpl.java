package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author atguigu-mqx
 */
@Service
public class ItemServiceImpl implements ItemService {

    //  做数据汇总：service-product 这个微服务！
    //  需要使用远程调用：

    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map<String, Object> map = new HashMap<>();
        /*
        1.	查询sku的基本信息
        2.	查询skuImage
        3.	分类信息
        4.	spu销售属性，属性值
        5.	查询最新价格
         */
        //  返回数据
        return map;
    }
}
