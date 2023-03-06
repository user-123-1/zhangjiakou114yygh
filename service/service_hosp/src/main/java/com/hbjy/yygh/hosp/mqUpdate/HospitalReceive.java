package com.hbjy.yygh.hosp.mqUpdate;

import com.hbjy.rabbitmq.constant.MqConst;
import com.hbjy.rabbitmq.service.RabbitMQService;
import com.hbjy.yygh.hosp.service.ScheduleService;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.msm.MsmVo;
import com.hbjy.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalReceive {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitMQService rabbitMQService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        //下单成功更新预约数
        Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        scheduleService.updateAvail(schedule);
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            rabbitMQService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
            System.out.println("已经执行完成更新号源数量操作，即将执行发送短信操作！");
        }
    }



}
