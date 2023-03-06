package com.hbjy.yygh.msg.msSendMsg;

import com.alibaba.nacos.client.utils.JSONUtils;
import com.hbjy.rabbitmq.constant.MqConst;
import com.hbjy.yygh.msg.service.MsgService;
import com.hbjy.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
* rabbitmq监听通道，如果有消息，则直接选择发送消息
* */
@Component
public class MsgReceive {
    @Autowired
    private MsgService msgService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(String json, Message message, Channel channel) throws IOException {

        MsmVo msmVo = (MsmVo) JSONUtils.deserializeObject(json,MsmVo.class);
        System.out.println(msmVo);
        msgService.mqSend(msmVo);
    }

}
