package com.hbjy.yygh.user.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbjy.yygh.common.authutils.AuthContextHolder;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.model.user.UserInfo;
import com.hbjy.yygh.user.service.UserInfoService;
import com.hbjy.yygh.vo.user.LoginVo;
import com.hbjy.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;
    //用户手机号登录的逻辑
    @ApiOperation(value = "手机登录逻辑")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> result = userInfoService.loginUser(loginVo);
        return Result.ok(result);
    }


    //测试类，以后要删除
    @GetMapping("/test/{phone}")
    public void testRedis(@PathVariable String phone) throws InterruptedException {
        String value = (String)redisTemplate.opsForValue().get(phone);
        System.out.println(value);
    }

    //用户认证接口
    @PostMapping("/auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){

        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口

    @GetMapping("/auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}
