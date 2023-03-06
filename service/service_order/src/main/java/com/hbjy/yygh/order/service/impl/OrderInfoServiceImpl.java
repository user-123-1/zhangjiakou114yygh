package com.hbjy.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.rabbitmq.constant.MqConst;
import com.hbjy.rabbitmq.service.RabbitMQService;
import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.result.ResultCodeEnum;
import com.hbjy.yygh.enums.OrderStatusEnum;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.model.user.Patient;
import com.hbjy.yygh.order.mapper.OrderInfoMapper;
import com.hbjy.yygh.order.service.OpenFeignService;
import com.hbjy.yygh.order.service.OpenFeignService2;
import com.hbjy.yygh.order.service.OrderInfoService;
import com.hbjy.yygh.vo.hosp.ScheduleOrderVo;
import com.hbjy.yygh.vo.msm.MsmVo;
import com.hbjy.yygh.vo.order.OrderMqVo;
import com.hbjy.yygh.vo.order.OrderQueryVo;
import com.hbjy.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Resource
    private OrderInfoMapper orderMapper;
    @Autowired
    private OpenFeignService openFeignService;
    @Autowired
    private OpenFeignService2 openFeignService2;
    @Resource
    private RabbitMQService rabbitMQService;//用于往交换机中发送消息
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //根据就诊人id获取就诊人基本信息(需要远程调用service-user)
        Patient patient = openFeignService.getPatientById(patientId);
        //根据排班id获取 医院、科室基本信息(需要远程调用service-hosp)
        ScheduleOrderVo scheduleOrderVo = openFeignService2.getScheduleOrderVo(scheduleId);

        //判断当前时间是否可预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()||new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        //获取签名信息
        SignInfoVo signInfoVo = openFeignService2.getSignInfoVo(scheduleOrderVo.getHoscode());
        //添加到订单表中
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        //像orderInfo中设置其他数据
        String outTradeNo = System.currentTimeMillis()+""+new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);//???? 有问题
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        int number = scheduleOrderVo.getAvailableNumber();
        orderInfo.setNumber(number-1);
        orderInfo.setFetchTime("20:00前");
        orderInfo.setFetchAddress("具体到医院安排");
        orderMapper.insert(orderInfo);

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderInfo.getOutTradeNo());
        OrderInfo one = orderMapper.selectOne(queryWrapper);
        OrderInfo o = new OrderInfo();
        o.setId(one.getId());
        o.setHosRecordId(one.getId()+"");
        orderMapper.updateById(o);

        //挂号完成之后，我们需要减少可预约号的数量（availableNumber） 并且发送短信，告诉预约成功 使用mq TODO
        //新建rabbitmq-util模块，然后在msg中引入，因为要发送短信
        //需要在msg中设置rabbitmq监听，如果有消息，则触发发送短信的接口
        //需要在hosp中也设置rabbitmq的监听，如果有消息，则执行可预约数量减少的操作！

        //发送mq消息，更新号源头
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(scheduleId);
        //orderMqVo.setReservedNumber();
        orderMqVo.setAvailableNumber(orderInfo.getNumber());

        //发送短信
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        msmVo.setTemplateCode("test");
        Map<String, Object> param = new HashMap<>();
        //System.out.println(param);
        param.put("code","666666");
        msmVo.setParam(param);
        orderMqVo.setMsmVo(msmVo);
        /*Map<String,Object> msmParam = msmVo.getParam();
        msmParam.put("code","您已经成功预约"+orderInfo.getHosname()+orderInfo.getDepname()+"服务");
        orderMqVo.setMsmVo(msmVo);*/

        rabbitMQService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

        return one.getId();
    }
    //根据id获取订单详情
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = orderMapper.selectById(orderId);

        return this.packageOrderInfo(orderInfo);
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = orderMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packageOrderInfo(item);
        });
        return pages;


    }

    @Override
    public Map<String, Object> show(Long id) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packageOrderInfo(this.getOrder(id+""));
        map.put("orderInfo", orderInfo);
        Patient patient
                =  openFeignService.getPatientById(orderInfo.getPatientId());
        map.put("patient", patient);
        return map;

    }

    @Override
    public OrderInfo getById(Long orderId) {
        OrderInfo orderInfo = orderMapper.selectById(orderId);
        return orderInfo;
    }

    private OrderInfo packageOrderInfo(OrderInfo orderInfo){
        Map<String,Object> map = new HashMap<>();
        map.put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        orderInfo.setParam(map);
        return orderInfo;
    }
}
