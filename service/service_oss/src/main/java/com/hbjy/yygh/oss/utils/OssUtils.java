package com.hbjy.yygh.oss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OssUtils implements InitializingBean {
    @Value("${aliyun.oss.regionId}")
    private String regionId;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.secret}")
    private String secret;

    public static String REGION_Id;//发送服务器的阿里云地址 设置为default即可
    public static String ACCESS_KEY_ID;
    public static String SECRECT;

    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_Id=regionId;
        ACCESS_KEY_ID=accessKeyId;
        SECRECT=secret;
    }
}
