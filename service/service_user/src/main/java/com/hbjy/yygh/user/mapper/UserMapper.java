package com.hbjy.yygh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbjy.yygh.model.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {
}
