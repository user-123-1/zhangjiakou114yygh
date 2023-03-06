package com.hbjy.yygh.hosp.controller;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.hosp.service.ScheduleService;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.ScheduleOrderVo;
import com.hbjy.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    //根据医院编号和科室编号，查询排班数据

    @ApiOperation(value = "查询排班数据")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getSchedule(@PathVariable long page,@PathVariable long limit,@PathVariable String hoscode,@PathVariable String depcode){
        Map<String,Object> map = scheduleService.getScheduleRule(page,limit,hoscode,depcode);
        return Result.ok(map);

    }
    //根据医院编号、科室编号和工作日期，查询出排班的详细信息
    @ApiOperation(value = "根据医院编号、科室编号和工作日期，查询出排班的详细信息")
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable String hoscode,@PathVariable String depcode,@PathVariable String workDate){
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return Result.ok(list);
    }
    @GetMapping("/getAllDocName/{hoscode}")
    //查出所有医生的姓名
    public Result getAllDocName(@PathVariable String hoscode){
        Set<String> docNameList = scheduleService.getAllDocName(hoscode);
        return Result.ok(docNameList);
    }
    //根据部门编号和医生姓名科室查询排班信息
    @GetMapping("/getScheByDepcodeAndName/{hoscode}/{depcode}/{docname}")
    public Result getScheduleByNameAndDepcode(@PathVariable String hoscode,@PathVariable String depcode,@PathVariable String docname){
        List<Schedule> scheduleList = scheduleService.getScheByNameAndDepcode(hoscode,depcode,docname);
        return Result.ok(scheduleList);
    }
    //根据对象删除具体的排班
    @DeleteMapping("/deleteDetailSchedule")
    public Result deleteDetailSchedule(@RequestBody Schedule schedule){
        scheduleService.deleteDetailSchedule(schedule);
        return Result.ok();
    }

    //给service-oder远程调用使用，根据排班id获取详细信息
    @GetMapping("/getDetailByScheduleId/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }
}
