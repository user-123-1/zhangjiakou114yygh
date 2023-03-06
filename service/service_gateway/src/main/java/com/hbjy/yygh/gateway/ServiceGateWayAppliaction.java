package com.hbjy.yygh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceGateWayAppliaction {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGateWayAppliaction.class,args);
    }
}
