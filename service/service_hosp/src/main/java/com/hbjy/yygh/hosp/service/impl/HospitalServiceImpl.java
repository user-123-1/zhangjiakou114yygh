package com.hbjy.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hbjy.yygh.hosp.mapper.HospitalSetMapper;
import com.hbjy.yygh.hosp.repository.HospitalRepository;
import com.hbjy.yygh.hosp.service.HospitalService;
import com.hbjy.yygh.hosp.service.HospitalSetService;
import com.hbjy.yygh.hosp.service.OpenFeignService;
import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.model.hosp.HospitalSet;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private OpenFeignService openFeignService;
    @Resource
    private HospitalSetMapper setMapper;

    @Override
    public void save(Hospital hospital) {
        String hoscode = hospital.getHoscode();
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        //如果不存在 则添加
        if (hospitals.isEmpty()){
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            mongoTemplate.insert(hospital);
        }else { //如果存在则修改
            hospital.setIsDeleted(0);
            hospital.setUpdateTime(new Date());
            hospital.setStatus(0);
            mongoTemplate.save(hospital);
        }
    }

    //根据医院编号查询mongodb
    @Override
    public Hospital getByHoscode(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        return hospitals.get(0);
    }

    //医院列表查询带分页
    @Override
    public Page<Hospital> selectHospPage(int page, int limit, HospitalQueryVo hospitalQueryVo) {
        Pageable pageable = PageRequest.of(page-1,limit);
        //创建条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        Example<Hospital> example = Example.of(hospital,matcher);
        //调用方法
        Page<Hospital> pages = hospitalRepository.findAll(example,pageable);
        List<Hospital> content = pages.getContent();
        //将医院等级等信息封装到param参数中！
        for (Hospital h:content){
            this.setHospitalType(h);
        }
        return pages;
    }



    //获取查询list集合，进行医院遍历的封装
    private void setHospitalType(Hospital h){
        //查询省、市、区
        String provinceString = openFeignService.getName(h.getProvinceCode());
        String cityString = openFeignService.getName(h.getCityCode());
        String districtString = openFeignService.getName(h.getDistrictCode());
        //查寻医院等级
        String hostypeString = openFeignService.getName("Hostype",h.getHostype());
        h.getParam().put("fullAddress",provinceString+cityString+districtString);
        h.getParam().put("hostypeString",hostypeString);

    }
    //跟新医院的上线状态
    @Override
    public void updateStatus(String id,Integer status) {
        //先根据id查询出医院的

        //先查询出医院的值
        Query query = new Query(Criteria.where("id").is(id));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        //设置修改的值
        hospitals.get(0).setStatus(status);
        hospitals.get(0).setUpdateTime(new Date());
        mongoTemplate.save(hospitals.get(0));
        //修改医院的基本信息
        UpdateWrapper<HospitalSet> wrapper = new UpdateWrapper<>();
        wrapper.set("status",hospitals.get(0).getStatus()).eq("hoscode",hospitals.get(0).getHoscode());
        setMapper.update(null,wrapper);


    }
    //显示医院的详细信息
    @Override
    public Map<String,Object> getHostById(String id) {
        //查出医院信息
        Query query = new Query(Criteria.where("id").is(id));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        //调用这个方法的目的是将 地址信息和医院等级信息单独放到属性param中，方便操作
        this.setHospitalType(hospitals.get(0));

        Map<String,Object> map = new HashMap<>();
        //医院基本信息（包含医院等级、省市区信息）
        map.put("hospital",hospitals.get(0));
        map.put("bookingRule",hospitals.get(0).getBookingRule());
        return map;
    }

    //删除医院的全部信息（包括排班、科室）
    @Override
    public void deleteHospital(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        mongoTemplate.remove(query,Hospital.class);//删除详细信息
        mongoTemplate.remove(query, Department.class);//删除科室
        mongoTemplate.remove(query, Schedule.class);//删除排班
    }

    @Override//根据医院名字进行模糊查询
    public List<Hospital> findByHosName(String hosname) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*"+hosname+".*$",Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where("hosname").regex(pattern));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        return hospitals;
    }

    @Override//根据医院编号获取BookingRule规则
    public Map<String, Object> finHosDetail(String hoscode) {
        Map<String,Object> result = new HashMap<>();
        Hospital byHoscode = this.getByHoscode(hoscode);
        this.setHospitalType(byHoscode);
        result.put("hospital",byHoscode);
        result.put("bookingRule",byHoscode.getBookingRule());
        byHoscode.setBookingRule(null);
        return result;
    }
    //获取医院名称
    @Override
    public String getHospName(String hoscode) {
        /*Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital != null) {
            return hospital.getHosname();
        }*/
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        if (!hospitals.isEmpty()){
            return hospitals.get(0).getHosname();
        }
        return null;
    }

}
