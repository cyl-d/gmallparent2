package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author cyl
 */
@Api(tags = "检索页面")
@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    //  创建对应的映射器
    @GetMapping("createIndex")
    public Result createIndex(){
        //  6.8.1 需要手动创建
        restTemplate.createIndex(Goods.class);  //  创建索引！
        restTemplate.putMapping(Goods.class);   //  创建mapping！
        return Result.ok();
    }

    @ApiOperation("上架")
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    @ApiOperation("下架")
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    @ApiOperation("商品的热度排名")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }

    @ApiOperation("检索数据控制器")
    @PostMapping
    public Result getList(@RequestBody SearchParam searchParam){
        //  调用服务层方法
        SearchResponseVo responseVo = searchService.search(searchParam);
        return Result.ok(responseVo);
    }

}
