package com.hbjy.yygh.order.controller.backController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.enums.OrderStatusEnum;
import com.hbjy.yygh.model.order.OrderInfo;
import com.hbjy.yygh.order.service.OrderInfoService;
import com.hbjy.yygh.vo.order.OrderQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderController {
    @Autowired
    private OrderInfoService orderInfoService;
    @GetMapping("{page}/{limit}")
    public Result index(
            @PathVariable Long page,
            @PathVariable Long limit,
             OrderQueryVo orderQueryVo) {
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderInfoService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @GetMapping("/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }
    //根据id获取订单详情
    @GetMapping("/show/{id}")
    public Result get(
            @PathVariable Long id) {
        return Result.ok(orderInfoService.show(id));
    }



}
