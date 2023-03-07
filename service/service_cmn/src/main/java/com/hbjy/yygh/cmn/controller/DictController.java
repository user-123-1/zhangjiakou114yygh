package com.hbjy.yygh.cmn.controller;

import com.hbjy.yygh.cmn.service.DictService;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")

public class DictController {
    @Autowired
    private DictService service;

    //根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表test,新建的分支加入的内容。")
    @GetMapping("/findChildData/{id}")
    public Result findChildrenData(@PathVariable Long id) {
        List<Dict> list = service.findChildById(id);
        return Result.ok(list);
    }

    //导出数据字典的接口
    @GetMapping("/exportDict")
    public void exportDict(HttpServletResponse response) {
        service.exportDictData(response);//传入response参数目的是下载专用
    }

    //导入数据的接口
    @PostMapping("/importDict")
    public void importDict(MultipartFile file) {
        service.importDictData(file);
    }

    //根据dictcode和value查询 医院等级
    @GetMapping("/getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,@PathVariable String value){
        String dictName = service.getDictName(dictCode,value);
        return dictName;
    }
    //根据value查询 医院等级
    @GetMapping("/getName/{value}")
    public String getName(@PathVariable String value){
        String dictName = service.getDictName("",value);
        return dictName;
    }
    //根据dict_code取下级节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode){
        List<Dict> list = service.findByDictCode(dictCode);
        return Result.ok(list);
    }
}
