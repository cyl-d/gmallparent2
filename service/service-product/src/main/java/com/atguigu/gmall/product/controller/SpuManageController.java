package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author atguigu-mqx
 */
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    //  springmvc 对象传值方式：
    //  注意参数传递的方式！
    //  http://localhost/admin/product/1/10?category3Id=62
    //  http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
    //  获取category3Id 对应的数据
    @GetMapping("{page}/{limit}")
    public Result getPage(@PathVariable Long page,
                          @PathVariable Long limit,
                          SpuInfo spuInfo){
        //  创建一个Page对象
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        //  调用服务层方法
        //  IPage<SpuInfo> page1 = manageService.getPage(spuInfoPage, spuInfo.getCategory3Id());
        IPage<SpuInfo> pages = manageService.getPages(spuInfoPage, spuInfo);
        //  设置返回数据
        return Result.ok(pages);
    }
}
