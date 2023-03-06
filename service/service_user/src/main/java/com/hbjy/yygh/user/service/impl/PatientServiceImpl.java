package com.hbjy.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hbjy.yygh.enums.DictEnum;
import com.hbjy.yygh.model.user.Patient;
import com.hbjy.yygh.user.mapper.PatientMapper;
import com.hbjy.yygh.user.service.OpenFeignService;
import com.hbjy.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private OpenFeignService openFeignService;
    @Override//获取当前登录的用户，查询出所有就诊人的列表（当前用户可以将家人添加就诊人）
    public List<Patient> findAllUserId(Long userId) {
        //根据userid查询出所有就诊人列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patients = patientMapper.selectList(wrapper);
        //通过远程调用数据字典，动态获取 省市区 证件类型等信息（因为省市区 证件类型等都是编号，而不是字符串）
        patients.stream().forEach(item->{
            //其他参数的封装
            this.packagePatient(item);
        });
        return patients;
    }

    private Patient packagePatient(Patient patient){
        //先根据dictCode为CertificatesType查询出id ，然后将这个id当成parent_id和code值进行查询具体的证件类型
        //获取具体值，为身份证或者是户口本
        String certificatesTypeString = openFeignService.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        //联系人证件类型
        String contactsCertificatesTypeString =
                openFeignService.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        //省
        String provinceString = openFeignService.getName(patient.getProvinceCode());
        //市
        String cityString = openFeignService.getName(patient.getCityCode());
        //区
        String districtString = openFeignService.getName(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }
    @Override//添加就诊人
    public void save(Patient patient) {
        patientMapper.insert(patient);
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = patientMapper.selectById(id);
        this.packagePatient(patient);
        return patient;
    }

    @Override
    public void updateById(Patient patient) {
        patientMapper.updateById(patient);
    }

    @Override
    public void removeById(Long id) {
        patientMapper.deleteById(id);
    }
}
