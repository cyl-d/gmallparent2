package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @author atguigu-mqx
 */
public interface ManageService {

    /**
     * 查询所有一级分类数据
     * @return
     */
    List<BaseCategory1> getBaseCategory1();

    /**
     * 根据一级分类Id 获取二级分类数据
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getBaseCategory2(Long category1Id);

    /**
     * 根据二级分类Id 查询三级分类数据
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getBaseCategory3(Long category2Id);

    /**
     * 根据一级分类Id，二级分类Id，三级分类id 查询平台属性列表！
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoList(Long category1Id,Long category2Id,Long category3Id);

    /**
     * 保存平台属性数据
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性Id 获取到平台属性值集合数据
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 通过平台属性Id 获取到平台属性对象
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * 根据三级分类Id 查询spuInfo列表
     * @param category3Id
     * @return
     */
    IPage<SpuInfo> getPage(Page<SpuInfo> page, Long category3Id);

    /**
     * 根据三级分类Id 查询spuInfo列表
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getPages(Page<SpuInfo> page,SpuInfo spuInfo);


}
