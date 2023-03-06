package com.hbjy.yygh.user.controller.apiwx;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.jwt.JwtHelper;
import com.hbjy.yygh.common.result.ResultCodeEnum;
import com.hbjy.yygh.model.user.UserInfo;
import com.hbjy.yygh.user.service.UserInfoService;
import com.hbjy.yygh.user.wxUtils.ConstWxUtils;
import com.hbjy.yygh.user.wxUtils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ucenter/wx")
public class WXApiController2 {
    @Autowired
    private UserInfoService userInfoService;
    @GetMapping("callback")
    public String callback(String code,String state) throws UnsupportedEncodingException {
        //获取临时票据
        System.out.println("code"+code);
        System.out.println("state = " + state);
        //第二步 拿着code和微信id和密钥， 请求微信固定地址，得到两个值
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            //log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        //使用code和appid以及appscrect换取access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")//带上上来请求过来的code
                .append("&grant_type=authorization_code");
        //向其中上面StringBuilder中拼接的东西注入值
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstWxUtils.WX_OPEN_APP_ID,
                ConstWxUtils.WX_OPEN_APP_SECRET,
                code);

        String result = null;
        try {
            //通过工具类发送上面拼接好的请求地址
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        System.out.println("使用code换取的access_token结果 = " + result);
        //将请求的回来的json 字符串转换为对象
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            //log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        //通过转换的对象，取出其中的值 access_token 和openid
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        //log.info(accessToken);
        //log.info(openId);

        //判断数据库中是否已经存在微信扫描人的信息
        UserInfo userInfo = userInfoService.selectWxOpenId(openId);
        if (userInfo==null){//如果数据库中不存在
            //通过access_token 和openid 继续拼接请求，然后发送请求
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String resultUserInfo = null;
            try {
                //将上面拼接的请求进行发送
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
            } catch (Exception e) {
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);
            //将请求来的字符串进行转换为对象处理
            JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
            if(resultUserInfoJson.getString("errcode") != null){
                //log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }

            //解析用户信息  从上面转换的对象获取用户信息
            //获取的信息包括openid 昵称  语言 省、市  国家  头像地址
            String nickname = resultUserInfoJson.getString("nickname");
            System.out.println("nickname是："+nickname+"!!!!!!!!!!!!!!!!");
            String headimgurl = resultUserInfoJson.getString("headimgurl");
            //获取的信息，调用方法将其存储到数据库中
            userInfo = new UserInfo();
            userInfo.setOpenid(openId);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);

        }


        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //目的是为了微信扫码之后绑定手机号用的
        //如果openid为空，不需要绑定手机号，如果不为空，需要绑定手机号
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        //创建token  和手机登录基本一样 获取id和name 装入token中
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        //localhost:3000/weixin/callback?token=xxx&openid=xxx 跳转
        return "redirect:" + ConstWxUtils.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+ URLEncoder.encode((String) map.get("name"),"utf-8");



    }
}
