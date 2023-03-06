package com.hbjy.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderInfoService {
    Long saveOrder(String scheduleId, Long patientId);

    OrderInfo getOrder(String orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    Map<String,Object> show(Long id);

    OrderInfo getById(Long orderId);
}
