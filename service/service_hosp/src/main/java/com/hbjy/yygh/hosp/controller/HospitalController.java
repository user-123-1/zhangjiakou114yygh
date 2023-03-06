package com.hbjy.yygh.hosp.controller;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.hosp.service.HospitalService;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/hosp/hospital")

public class HospitalController {
    @Autowired
    HospitalService hospitalService;

    //条件查询带分页
    @GetMapping("/list/{page}/{limit}")
    public Result listHosp(@PathVariable int page, @PathVariable int limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageModel = hospitalService.selectHospPage(page,limit,hospitalQueryVo);
        return Result.ok(pageModel);
    }

    //跟新医院的上线状态
    @ApiOperation(value = "更新医院的上线状态")
    @GetMapping("/updateHospitalStatus/{id}/{status}")
    public Result updateHospitalStatus(@PathVariable String id,@PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }
    //显示医院的详细信息
    @ApiOperation(value = "显示医院的详细信息")
    @GetMapping("/showHospitalDetal/{id}")
    public Result showHospitalDetail(@PathVariable String id){
        Map<String,Object> map = hospitalService.getHostById(id);
        return Result.ok(map);
    }

}
