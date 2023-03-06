package com.hbjy.yygh.common.authutils;


import com.hbjy.yygh.common.jwt.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//获取当前用户信息的工具类
public class AuthContextHolder {
    //获取用户id
    public static Long getUserId(HttpServletRequest request){
        //从header中获取token
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }
    //获取用户名称
    public static String getUserName(HttpServletRequest request){
        //从header中获取token
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
