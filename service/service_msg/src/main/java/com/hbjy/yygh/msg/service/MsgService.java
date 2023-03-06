package com.hbjy.yygh.msg.service;

import com.hbjy.yygh.vo.msm.MsmVo;

public interface MsgService {
    boolean send(String phone, String random);

    boolean mqSend(MsmVo msmVo);
}
