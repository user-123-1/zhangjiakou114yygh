package com.hbjy.yygh.order.service;

import com.hbjy.yygh.model.order.OrderInfo;

import java.util.Map;

public interface PaymentInfoService {
    void savePaymentInfo(OrderInfo order, Integer status);

    void paySuccess(String out_trade_no, Map<String, String> resultMap);
}
