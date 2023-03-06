package com.hbjy.yygh.hosp.controller.Api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.hospital_managerUtil.helper.HttpRequestHelper;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.common.result.ResultCodeEnum;
import com.hbjy.yygh.common.util.MD5;
import com.hbjy.yygh.hosp.service.DepartmentService;
import com.hbjy.yygh.hosp.service.HospitalService;
import com.hbjy.yygh.hosp.service.HospitalSetService;
import com.hbjy.yygh.hosp.service.ScheduleService;
import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.DepartmentQueryVo;
import com.hbjy.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    @Autowired
    HospitalService service;
    @Autowired
    HospitalSetService setService;
    @Resource
    DepartmentService departmentService;
    @Autowired
    ScheduleService scheduleService;

    //上传医院接口
    @PostMapping("/saveHospital")
    public Result saveHosp(@RequestBody Hospital hospital){

        service.save(hospital);
        return Result.ok();
    }
    //删除医院详细信息的接口(删除全部，包括排班、科室)
    @DeleteMapping("/removeHospital/{hoscode}")
    public Result deleteHospital(@PathVariable String hoscode){
        service.deleteHospital(hoscode);
        return Result.ok();
    }
    //删除科室的接口
    @DeleteMapping("/removeDepartment/{hoscode}")
    public Result deleteDepartment(@PathVariable String hoscode){
        departmentService.deleteDepartment(hoscode);
        return Result.ok();

    }
    @DeleteMapping("/removeDepartment2/{hoscode}/{depcode}")
    public Result deleteDepartment2(@PathVariable String hoscode,@PathVariable String depcode){
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }


    //上传科室接口
    @PostMapping("/saveDepartment")
    public Result saveDepartment(@RequestBody List<Department> departmentList){

        departmentService.save(departmentList);
        return Result.ok();
    }

    //上传排班的接口
    @PostMapping("/saveSchedule")
    public Result saveSchedule(@RequestBody List<Schedule> scheduleList){
        scheduleService.save(scheduleList);
        return Result.ok();

    }
    //删除全部排班的接口
    @DeleteMapping("/deleteSchedule/{hoscode}")
    public Result deleteSchedule(@PathVariable String hoscode){
        scheduleService.removeAllSchedule(hoscode);
        return Result.ok();
    }



    //删除部分排班的接口
    @DeleteMapping("/deleteSomeSchedule/{hoscode}/{}")

    //处理上传来的医院图标，将其转换为base64 字符串模式
    @PostMapping("/letPictureToString")
    public String getPictureToString(@RequestParam("file")MultipartFile multipartFile) throws IOException {
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String originalFilename = multipartFile.getOriginalFilename();
        System.out.println(originalFilename);
        String encode = base64Encoder.encode(multipartFile.getBytes());
        return encode;
    }

    
}
