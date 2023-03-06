package com.hbjy.yygh.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.authutils.AuthContextHolder;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.enums.OrderStatusEnum;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.order.service.OrderInfoService;
import com.hbjy.yygh.vo.order.OrderQueryVo;
import com.hbjy.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {
    @Autowired
    private OrderInfoService orderInfoService;
    //生成订单
    @PostMapping("/auth/submit/Order/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,@PathVariable Long patientId){
        Long orderId = orderInfoService.saveOrder(scheduleId,patientId);
        return Result.ok(orderId);
    }
    //根据订单id获取订单信息
    @GetMapping("/auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable String orderId){
        OrderInfo orderInfo = orderInfoService.getOrder(orderId);
        return Result.ok(orderInfo);
    }
    //订单列表
    @GetMapping("/auth/{page}/{limit}/{userId}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, @PathVariable Long userId, OrderQueryVo orderQueryVo){
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> pageParam = new Page<>(page,limit);
        IPage<OrderInfo> pageModel = orderInfoService.selectPage(pageParam,orderQueryVo);
        return Result.ok(pageModel);
    }
    @GetMapping("/auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

}
