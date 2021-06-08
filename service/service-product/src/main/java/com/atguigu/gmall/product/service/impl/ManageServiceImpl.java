package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author atguigu-mqx
 */
@Service
public class ManageServiceImpl implements ManageService {
    //  调用mapper层
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;



    @Override
    public List<BaseCategory1> getBaseCategory1() {
        //  select * from base_category1
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getBaseCategory2(Long category1Id) {
        //  select * from base_category2 where category1_id = ?
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id",category1Id));
    }

    @Override
    public List<BaseCategory3> getBaseCategory3(Long category2Id) {
        //  select * from base_category3 where category2_id = ?
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    @Override
    public List<BaseAttrInfo> getBaseAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //  select * from base_attr_info where category_id = category1Id and category_level = 1
        //  select * from base_attr_info where category_id = category2Id and category_level = 2
        //  select * from base_attr_info where category_id = category3Id and category_level = 3
        //  在此引出mybatis 中的动态sql标签！
        //  后续的功能中，有个需求：根据分类Id ，查询平台属性，平台属性值：
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
        //  返回数据
        return baseAttrInfoList;
    }

    //  该方法既有对平台属性的新增，也有对平台属性的修改！
    @Override
    @Transactional(rollbackFor = Exception.class)   //  在方法体内发生异常的时候，就会回滚！
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //  base_attr_info
        if (baseAttrInfo.getId()!=null){
            //  修改操作
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //  base_attr_value int i = 1/0; 先删除平台属性Id 对应的属性值数据，再做新增
        //  delete from base_attr_value where attr_id = ?
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueQueryWrapper);

        //  必须获取到平台属性值数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)){
            //  循环遍历添加
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //  base_attr_value.attr_id = base_attr_info.id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        //  select * from base_attr_value where attr_id = ?
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id", attrId));
        //  返回数据
        return baseAttrValueList;
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        //  根据主键查询平台属性对象
        //  select * from base_attr_info where id = attrId;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //  判断平台属性是否为空
        if (baseAttrInfo!=null){
            //  select * from base_attr_value where attr_id = ?
            baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        }
        //  返回平台属性对象
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getPage(Page<SpuInfo> page, Long category3Id) {
        //  封装条件
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",category3Id);
        spuInfoQueryWrapper.orderByDesc("id");
        //  需要两个参数，一个是page 当前页，每页显示的条数，第二个Wrapper 条件
        Page<SpuInfo> spuInfoPage = spuInfoMapper.selectPage(page, spuInfoQueryWrapper);
        return spuInfoPage;
    }

    @Override
    public IPage<SpuInfo> getPages(Page<SpuInfo> page,SpuInfo spuInfo) {
        //  封装条件
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        spuInfoQueryWrapper.orderByDesc("id");
        //  需要两个参数，一个是page 当前页，每页显示的条数，第二个Wrapper 条件
        Page<SpuInfo> spuInfoPage = spuInfoMapper.selectPage(page, spuInfoQueryWrapper);
        return spuInfoPage;
    }

    @Override
    public List<BaseSaleAttr> getBseSaleAttrList() {
        //  select * from base_sale_attr
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);

        return baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
            spuInfo;
            spuImage;
            spuSaleAttr;
            spuSaleAttrValue;
         */
        spuInfoMapper.insert(spuInfo);

        //  获取到spuImage 集合数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //  判断不为空
        if (!CollectionUtils.isEmpty(spuImageList)){
            //  循环遍历
            for (SpuImage spuImage : spuImageList) {
                //  需要将spuId 赋值
                spuImage.setSpuId(spuInfo.getId());
                //  保存spuImge
                spuImageMapper.insert(spuImage);
            }
        }
        //  获取销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //  判断
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            //  循环遍历
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //  需要将spuId 赋值
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //  再此获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //  判断
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    //  循环遍历
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //   需要将spuId， sale_attr_name 赋值
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        //  select * from spu_image where spu_id = ?
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id",spuId);
        return spuImageMapper.selectList(spuImageQueryWrapper);

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        //  不仅需要获取到销售属性名，还需要获取到销售属性值名称！ spu_sale_attr ， spu_sale_attr_value
        List<SpuSaleAttr>  spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        skuInfo
        skuAttrValue
        skuSaleAttrValue
        skuImage
         */
        //  保存skuInfo
        skuInfoMapper.insert(skuInfo);
        //  skuAttrValue
        //  获取数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //  判断
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            //  循环遍历
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                //  给skuId 赋值
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        //  先获取到对应的数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            //  循环遍历
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //  sku_id
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                //  spu_id
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
        //   skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            //  循环遍历
            for (SkuImage skuImage : skuImageList) {
                //  sku_id
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public IPage<SkuInfo> getSkuInfoList(Page<SkuInfo> skuInfoPage) {
        //  查询的时候按照id 进行降序排列
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage,skuInfoQueryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        //  本质： is_sale = 1
        //  update sku_info set is_sale = 1 where id = skuId
        //  需要的是实体类：实体类中一定得有主键！

        //        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //        skuInfo.setIsSale(1);
        //        skuInfoMapper.updateById(skuInfo);
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);


    }

    @Override
    public void cancelSale(Long skuId) {
        //  本质： is_sale = 0
        //  update sku_info set is_sale = 0 where id = skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //  select * from sku_info where id = skuId;
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        //  可以将skuImageList 查询出来放入skuInfo 对象！
        //  select * from sku_image where sku_id = skuId;
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));

        //  给当前的skuInfo 属性赋值
        skuInfo.setSkuImageList(skuImageList);
        //  返回数据
        return skuInfo;
    }


    @Override
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        //  select * from base_category_view where id = 61;
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        //  调用mapper 的方法
        /*
        select
            ssa.id,
            ssa.spu_id,
            ssa.base_sale_attr_id,
            ssa.sale_attr_name,
            ssav.id sale_attr_value_id,
            ssav.sale_attr_value_name,
            if(sav.sku_id is null,0,1) is_checked #
        from spu_sale_attr ssa inner join spu_sale_attr_value  ssav
            on  ssa.spu_id = ssav.spu_id and ssa.base_sale_attr_id = ssav.base_sale_attr_id
            left join sku_sale_attr_value sav on sav.sale_attr_value_id = ssav.id and sku_id = 44
        where ssa.spu_id = 27
        order by ssa.base_sale_attr_id,ssav.id;
         */
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //  select price from sku_info where id = skuId;
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //  select * from sku_info where id = skuId;
        if (skuInfo!=null){
            return skuInfo.getPrice();
        }else {
            return new BigDecimal(0);
        }
    }
}
