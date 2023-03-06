package com.hbjy.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.hbjy.yygh.model.hosp.HospitalSet;
import com.hbjy.yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {
    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
