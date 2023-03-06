package com.hbjy.yygh.hosp.controller;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.hosp.service.DepartmentService;
import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/hosp/department")
@RestController

public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    //根据医院编号，查询所有科室列表
    @GetMapping("/getDepartmentList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode){
        List<DepartmentVo> departmentVoList = departmentService.findDeptTree(hoscode);
        return Result.ok(departmentVoList);
    }

    @GetMapping("/getDepartment/{hoscode}")
    //根据医院编号，查询所有科室列表（非树型结构）
    public Result getDep(@PathVariable String hoscode){
        List<Department> departments = departmentService.findDepartment(hoscode);
        return Result.ok(departments);
    }
}
