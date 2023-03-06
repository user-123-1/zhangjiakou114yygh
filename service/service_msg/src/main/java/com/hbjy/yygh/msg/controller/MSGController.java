package com.hbjy.yygh.msg.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.msg.service.MsgService;
import com.hbjy.yygh.msg.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/msg")
public class MSGController {
    @Autowired
    private MsgService msgService;
    @Autowired
    private RedisTemplate redisTemplate;



    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable String phone){
        //从reids中获取验证码，如果ok 则返回ok
        //key是手机号 value是验证码
        String code = (String) redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)){
            return Result.ok();
        }
        //如果获取不到，则进行阿里云进行发送短信
        //调用工具类生成6位验证码
        String random = RandomUtil.getSixBitRandom();
        System.out.println(random+"!!!!!!!!!!!!!!!!!!!");
        boolean isSend = msgService.send(phone,random);
        if (isSend){
            //存入数据库中，手机号为key  验证码为value， 时间为5分组有效
            redisTemplate.opsForValue().set(phone,random,5, TimeUnit.MINUTES);
            return Result.ok();
        }else {
            return Result.fail().message("发送短信失败");
        }

    }

    @GetMapping("/testRedis/{phone}/{code}")
    public void testRedis(@PathVariable String phone,@PathVariable String code){
        redisTemplate.opsForValue().set(phone,code,5,TimeUnit.MINUTES);
    }
}
