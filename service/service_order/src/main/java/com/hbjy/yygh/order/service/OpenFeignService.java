package com.hbjy.yygh.order.service;

import com.hbjy.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(value = "service-user")
public interface OpenFeignService {
    @GetMapping("/user/patient/openFeign/getPatient/{id}")
    public Patient getPatientById(@PathVariable Long id);

}
