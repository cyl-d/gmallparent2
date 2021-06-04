package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;

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


}
