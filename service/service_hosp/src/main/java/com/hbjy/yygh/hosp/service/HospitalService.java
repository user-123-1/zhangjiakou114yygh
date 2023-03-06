package com.hbjy.yygh.hosp.service;

import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Hospital hospital);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> selectHospPage(int page, int limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id,Integer status);

    Map<String,Object> getHostById(String id);

    void deleteHospital(String hoscode);


    List<Hospital> findByHosName(String hosname);

    //获取医院名称
    String getHospName(String hoscode);

    //根据医院编号获取BookingRule规则
    Map<String, Object> finHosDetail(String hoscode);


}
