package com.hbjy.yygh.user.controller.patient;

import com.hbjy.yygh.common.authutils.AuthContextHolder;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.model.user.Patient;
import com.hbjy.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


//就诊人列表接口
@RestController
@RequestMapping("/user/patient")
public class PatientController {
    @Autowired
    private PatientService patientService;

    //获取就诊人的接口
    @GetMapping("/auth/findAll")
    public Result findAll(HttpServletRequest request){
        //获取当前登陆人的id值
        Long userId = AuthContextHolder.getUserId(request);
        //返回一个list集合，因为一个用户中可能有多个就诊人的信息（可以添加家人）
        List<Patient> patientList = patientService.findAllUserId(userId);
        return Result.ok(patientList);
    }

    //添加就诊人的接口
    @PostMapping("/auth/save")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request){
        //获取当前用户登录的id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();

    }

    //根据id获取就诊人信息
    @GetMapping("/auth/get/{id}")
    public Result getPatient(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    //修改就诊人
    @PostMapping("/auth/update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }

    //删除就诊人
    @DeleteMapping("/auth/remove/{id}")
    public Result removePatientById(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }
    //根据patientId获取就诊人信息
    @GetMapping("/openFeign/getPatient/{id}")
    public Patient getPatientById(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }
}
