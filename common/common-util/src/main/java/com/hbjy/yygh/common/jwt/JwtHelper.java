package com.hbjy.yygh.common.jwt;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.jsonwebtoken.*;

import java.util.Date;

public class JwtHelper {
    //过期时间
    private static long tokenExpiration = 24*60*60*1000;
    //前面密钥
    private static String tokenSignKey = "123456";
    //根据用户id和用户姓名生成token
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()//设置头部

                //设置主题和过期时间
                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("userName", userName)
                //设置加密签名
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    //根据token生成用户id
    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }
    //根据token得到用户的用户名
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }
   /* public static void main(String[] args) {
        String token = JwtHelper.createToken(1L, "55");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }*/
}

