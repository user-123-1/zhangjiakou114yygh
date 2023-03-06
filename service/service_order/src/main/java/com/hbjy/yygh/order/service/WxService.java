package com.hbjy.yygh.order.service;

import java.util.Map;

public interface WxService {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);
}
