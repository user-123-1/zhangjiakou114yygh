package com.hbjy.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.model.user.UserInfo;
import com.hbjy.yygh.vo.user.LoginVo;
import com.hbjy.yygh.vo.user.UserAuthVo;
import com.hbjy.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService {
    Map<String, Object> loginUser(LoginVo loginVo);
    //保存从微信登录的用户信息
    void save(UserInfo userInfo);
    //通过openid 判断数据库中是否已经存在数据
    UserInfo selectWxOpenId(String openId);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    UserInfo getById(Long userId);

    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void updateStatus(Long userId, Integer status);

    Map<String, Object> show(long userId);

    void approval(Long userId, Integer authStatus);
}
