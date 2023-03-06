package com.hbjy.yygh.msg.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hbjy.yygh.msg.service.MsgService;
import com.hbjy.yygh.msg.utils.MSGUtils;
import com.hbjy.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsgServiceImpl implements MsgService {
    @Override
    public boolean send(String phone, String random) {
        if (StringUtils.isEmpty(phone)){
            return false;
        }
        DefaultProfile profile =
                DefaultProfile.getProfile("default", MSGUtils.ACCESS_KEY_ID, MSGUtils.SECRECT);
        IAcsClient client = new DefaultAcsClient(profile);


        //设置相关固定的参数
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        //设置发送相关的参数
        request.putQueryParameter("PhoneNumbers",phone); //手机号
        request.putQueryParameter("SignName","阿里云短信测试"); //申请阿里云 签名名称
        request.putQueryParameter("TemplateCode","SMS_154950909"); //申请阿里云 模板code
        Map<String,Object> param = new HashMap<>();
        param.put("code",random);//为得是转换为json类的数据
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param)); //验证码数据，转换json数据传递

        try {
            //最终发送
            CommonResponse response = client.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            return success;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //MQ发送短信的封装
    @Override
    public boolean mqSend(MsmVo msmVo) {
        //如果手机号不为空才选择发送
        if (!StringUtils.isEmpty(msmVo.getPhone())){
            String code = (String) msmVo.getParam().get("code");//获取验证码
            System.out.println(code+"!!!!!!!!!!!!!!!!!!!!!");
            boolean isSend = this.send(msmVo.getPhone(),code);
            System.out.println("发送成功？"+isSend);
            return isSend;
        }
        return false;
    }
}
