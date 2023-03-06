package com.hbjy.yygh.order.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.hbjy.yygh.enums.PaymentTypeEnum;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.order.mapper.PaymentInfoMapper;
import com.hbjy.yygh.order.service.OrderInfoService;
import com.hbjy.yygh.order.service.PaymentInfoService;
import com.hbjy.yygh.order.service.WxService;
import com.hbjy.yygh.order.wxPayUtils.ConstantPropertiesUtils;
import com.hbjy.yygh.order.wxPayUtils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WxServiceImpl implements WxService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Override//生成微信支付二维码
    public Map createNative(Long orderId) {
        //从redis获取数据
        Map paymap = (Map) redisTemplate.opsForValue().get(orderId.toString());
        if (paymap!=null){
            return paymap;
        }

        //根据orderId获取订单信息
        OrderInfo order = orderInfoService.getOrder(orderId+"");
        //向支付表中添加信息
        paymentInfoService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
        //设置参数，调用微信生成二维码接口
        Map paramMap = new HashMap();
        //以下都是固定参数
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        String body = order.getReserveDate() + "就诊"+ order.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", order.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1");//为了测试统一写成一分钱
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");

        //调用微信生成二维码接口 httpClient工具
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        //client设置参数
        try {
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);//因为为https方式
            client.post();//因为为post方式，


            //微信官方返回相关数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));//生成的二维码地址

            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    //得到支付状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //根据orderId获取订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //封装提交参数（固定值）
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //设置请求内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
//3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;

        }catch (Exception e){
            return null;
        }

    }
}
