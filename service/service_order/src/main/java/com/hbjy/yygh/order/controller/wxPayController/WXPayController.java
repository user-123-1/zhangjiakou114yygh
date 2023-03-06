package com.hbjy.yygh.order.controller.wxPayController;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.order.service.PaymentInfoService;
import com.hbjy.yygh.order.service.WxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
public class WXPayController {
    @Autowired
    private WxService wxService;
    @Autowired
    private PaymentInfoService paymentInfoService;
    //生成微信支付二维码
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId){
        Map map = wxService.createNative(orderId);
        return Result.ok(map);
    }
    //查询支付状态
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId){
        //查询状态，调用微信接口，
        Map<String,String> resultMap =  wxService.queryPayStatus(orderId);
        if (resultMap==null){
            return Result.fail().message("支付出错！");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            //更新订单状态
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no,resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
