package com.hbjy.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.jwt.JwtHelper;
import com.hbjy.yygh.common.result.ResultCodeEnum;
import com.hbjy.yygh.enums.AuthStatusEnum;
import com.hbjy.yygh.model.user.Patient;
import com.hbjy.yygh.model.user.UserInfo;
import com.hbjy.yygh.user.mapper.UserMapper;
import com.hbjy.yygh.user.service.PatientService;
import com.hbjy.yygh.user.service.UserInfoService;
import com.hbjy.yygh.vo.user.LoginVo;
import com.hbjy.yygh.vo.user.UserAuthVo;
import com.hbjy.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PatientService patientService;
    @Override//用户登录的逻辑
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从loginVo中获得手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //判断手机号和验证码是否为空
        if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //判断手机验证码和输入的是否一致 TODO
        String redisCode = (String) redisTemplate.opsForValue().get(phone);
        System.out.println(redisCode+"从数据库中获取的验证码");
        if (!code.equals(redisCode)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //绑定手机号码(微信登录的时候用)
        //微信登录的时候，只有openid 没有手机号，需要将手机号加进去
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selectWxOpenId(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                userMapper.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        if (userInfo==null){//如果为空，则表示不是微信登录，进行正常的手机登录流程
            //判断是否第一次登录，根据手机号查询数据库，如果不存在手机号泽是第一次登录
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            userInfo = userMapper.selectOne(wrapper);
            if (userInfo==null){//第一次使用手机号登录
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                userMapper.insert(userInfo);
            }
            //校验是否被锁定  status1表示正常 0表示锁定
            if (userInfo.getStatus()==0){
                throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
            }
        }




        //不是第一次，直接登录
        //返回登录信息
        //返回登录用户名
        //返回token信息 TODO
        Map<String,Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)){
            name=userInfo.getName();
        }
        if (StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token",token);
        return map;
    }
    //保存从微信登录的用户信息
    @Override
    public void save(UserInfo userInfo) {
        userMapper.insert(userInfo);
    }

    //通过openid 判断数据库中是否已经存在数据
    @Override
    public UserInfo selectWxOpenId(String openId) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openId);
        UserInfo userInfo = userMapper.selectOne(queryWrapper);
        return userInfo;
    }

    @Override//用户认证
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = userMapper.selectById(userId);
        //设置认证信息
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //信息更新
        userMapper.updateById(userInfo);

    }

    @Override//根据id获取用户信息
    public UserInfo getById(Long userId) {
        UserInfo userInfo = userMapper.selectById(userId);
        return userInfo;
    }

    //后台管理接口，查询所有用户列表 包含模糊查询
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //通过userInfoQueryVo 获取条件值
        String name = userInfoQueryVo.getKeyword();//用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();//是否认证
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();//开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//结束时间
        //对条件值进行判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }
        if (!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status",authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_time",createTimeEnd);
        }
        Page<UserInfo> page = userMapper.selectPage(pageParam, wrapper);
        //将编号变成具体的值
        page.getRecords().stream().forEach(item->{
            this.packageUserInfo(item);
        });
        return page;
    }



    private UserInfo packageUserInfo(UserInfo userInfo){
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态
        String statusString = userInfo.getStatus().intValue()==0?"锁定":"正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }
    //修改用户的锁定状态
    @Override
    public void updateStatus(Long userId, Integer status) {

        UserInfo userInfo = userMapper.selectById(userId);
        userInfo.setStatus(status);
        userMapper.updateById(userInfo);

    }
    //用户详情信息
    @Override
    public Map<String, Object> show(long userId) {
        Map<String,Object> map = new HashMap<>();
        //根据userid查询就诊人信息
        UserInfo userInfo = this.packageUserInfo(userMapper.selectById(userId));
        map.put("userInfo",userInfo);
        //根据userid查询到就诊人信息（多个，一个人可以给家里人添加就诊人）
        List<Patient> allUserId = patientService.findAllUserId(userId);
        map.put("patientList",allUserId);
        return map;
    }

    //审核认证用户
    //认证审批 认证状态（0：未认证 1：认证中 2：认证成功 -1：认证失败
    @Override
    public void approval(Long userId, Integer authStatus) {
        //如果是2或者是-1 表示审核通过或者审核不通过
        if (authStatus.intValue()==2||authStatus==-1){
            UserInfo userInfo = userMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            userMapper.updateById(userInfo);
        }
    }

}
