package com.hbjy.yygh.user.controller.back;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.model.user.UserInfo;
import com.hbjy.yygh.user.service.UserInfoService;
import com.hbjy.yygh.vo.user.UserInfoQueryVo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//用户的后台管理接口，专门为后台服务
@RestController
@RequestMapping("/user/admin")
public class UserBackController {
    @Autowired
    private UserInfoService userInfoService;

    //用户列表接口 分页+模糊查询
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable long page, @PathVariable long limit, UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(pageParam,userInfoQueryVo);
        return Result.ok(pageModel);
    }
    //用户锁定
    @GetMapping("/lock/{userId}/{status}")
    public Result lockUser(@PathVariable Long userId, @PathVariable Integer status){
        userInfoService.updateStatus(userId,status);
        return Result.ok();
    }
    //用户详情
    @GetMapping("/show/{userId}")
    public Result show(@PathVariable long userId){
        Map<String,Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }
    //认证审批的接口
    @GetMapping("/approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus){
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }

}
