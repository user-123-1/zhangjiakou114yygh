package com.hbjy.yygh.order.service;

import com.hbjy.yygh.vo.hosp.ScheduleOrderVo;
import com.hbjy.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Component
@FeignClient(value = "service-hosp")
public interface OpenFeignService2 {
    @GetMapping("/admin/hosp/schedule/getDetailByScheduleId/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId);

    @GetMapping("/admin/hosp/hospitalSet/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode);
}
