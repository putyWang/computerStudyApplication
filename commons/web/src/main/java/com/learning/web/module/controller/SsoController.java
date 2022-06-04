package com.learning.web.module.controller;

import com.learning.core.utils.CommonBeanUtil;
import com.learning.core.utils.MD5Utils;
import com.learning.core.bean.ApiResult;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sso")
@Api(value = "单点登录相关类")
public class SsoController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    @ApiOperation(value = "登录接口")
    public ApiResult login (@Validated LoginDto loginDto) {

        return ApiResult.ok(userService.login(loginDto));
    }

    @PutMapping("/registered")
    @ApiOperation(value = "注册接口")
    public ApiResult registered (@Validated UserDto userDto) {

        UserEntity user = new UserEntity();
        UserEntity userEntity = CommonBeanUtil.copyAndFormat(user, userDto);
        String password = MD5Utils.encrypt(userDto.getPassword());
        userEntity.setPassword(password);
        userService.insert(userEntity);
        return ApiResult.ok("注册成功");
    }
}
