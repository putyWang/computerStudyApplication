package com.learning.commons.module.controller;

import com.learning.commons.entity.ApiResult;
import com.learning.commons.module.dto.LoginDto;
import com.learning.commons.module.service.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sso")
@Api(value = "单点登录相关类")
public class SsoController {

    @Autowired
    private TestService testService;

    @GetMapping("/login")
    @ApiOperation(value = "登录接口")
    public ApiResult login (@Validated @RequestBody LoginDto loginDto) {
        return ApiResult.ok("登录成功").setData("超级管理员");
    }
}
