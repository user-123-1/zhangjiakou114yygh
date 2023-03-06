package com.hbjy.yygh.msg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
public class ServiceMsgApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsgApplication.class,args);
    }
}
