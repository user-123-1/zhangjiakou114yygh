package com.hbjy.yygh.user.controller.apiwx;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.user.wxUtils.ConstWxUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//微信操作接口
@Controller
@RequestMapping("/user/ucenter/wx")
public class WXApiController {
    //1、生成微信二维码
    @GetMapping("/getLoginParam")
    @ResponseBody
    public Result getLoginParam() throws UnsupportedEncodingException {
        Map<String,Object> map = new HashMap<>();
        map.put("appid", ConstWxUtils.WX_OPEN_APP_ID);//从微信开放平台得到的appid
        map.put("scope","snsapi_login");//网页应用填入的固定值
        String wxOpenRedirectUrl = ConstWxUtils.WX_OPEN_REDIRECT_URL;
        String encode = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");//参数之一，表示跳转的uri
        map.put("redirect_uri",encode);
        map.put("state",System.currentTimeMillis());//不是必需的值
        return Result.ok(map);
    }

    //2、得到扫码人的信息
}
