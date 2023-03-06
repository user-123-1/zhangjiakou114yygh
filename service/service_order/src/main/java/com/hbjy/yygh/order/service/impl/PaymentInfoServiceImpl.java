package com.hbjy.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hbjy.yygh.enums.PaymentStatusEnum;
import com.hbjy.yygh.enums.PaymentTypeEnum;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.model.order.PaymentInfo;
import com.hbjy.yygh.order.mapper.PaymentInfoMapper;
import com.hbjy.yygh.order.service.PaymentInfoService;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    @Resource
    private PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        //根据订单id和支付类型，查询支付记录表是否存在相同的记录
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",order.getId());
        queryWrapper.eq("payment_type",paymentType);
        Integer integer = paymentInfoMapper.selectCount(queryWrapper);
        if (integer>0){
            return;
        }
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        paymentInfoMapper.insert(paymentInfo);
    }

    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        
    }
}
