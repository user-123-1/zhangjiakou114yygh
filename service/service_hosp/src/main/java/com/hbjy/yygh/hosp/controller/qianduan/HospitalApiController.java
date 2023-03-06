package com.hbjy.yygh.hosp.controller.qianduan;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.hosp.service.DepartmentService;
import com.hbjy.yygh.hosp.service.HospitalService;
import com.hbjy.yygh.hosp.service.ScheduleService;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.DepartmentVo;
import com.hbjy.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "查询医院列表功能")
    @GetMapping("/findHospitalList/{page}/{limit}")
    public Result findHosList(@PathVariable int page, @PathVariable int limit,HospitalQueryVo hospitalQueryVo ){
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);

        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据医院名称进行查询")
    @GetMapping("/findByHosName/{hosname}")
    public Result findByHosName(@PathVariable String hosname){
        //因为是做模糊查询，所以要返回一个集合
        List<Hospital> hospitalList = hospitalService.findByHosName(hosname);
        return Result.ok(hospitalList);
    }
    @ApiOperation(value = "根据医院编号获取所有科室")
    @GetMapping("/getDepartment/{hoscode}")
    public Result getDepartment(@PathVariable String hoscode){
        List<DepartmentVo> deptTree = departmentService.findDeptTree(hoscode);
        return Result.ok(deptTree);
    }
    @ApiOperation(value = "根据医院编号获取BookingRule规则")
    @GetMapping("/findHosDetail/{hoscode}")
    public Result findHosDetail(@PathVariable String hoscode){
        Map<String,Object> map = hospitalService.finHosDetail(hoscode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {
        return Result.ok(scheduleService.getScheduleDetail(hoscode, depcode, workDate));
    }
    @ApiOperation(value = "获取排班的id获取排班的数据")
    @GetMapping("/getSchedule/{scheduleId}")
    public Result getScheduleId(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getScheduleId(scheduleId);
        return Result.ok(schedule);
    }


}
