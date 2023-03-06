package com.hbjy.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.hbjy.yygh.user.mapper")
public class UserInfoConfig {
}
