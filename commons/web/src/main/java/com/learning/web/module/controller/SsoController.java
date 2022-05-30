package com.learning.web.module.controller;

import com.learning.shiro.bean.JwtToken;
import com.learning.core.bean.ApiResult;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.TestService;
import com.learning.core.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/sso")
@Api(value = "单点登录相关类")
public class SsoController {

    @Autowired
    private TestService testService;

    /**
     * Token密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    @GetMapping("/login")
    @ApiOperation(value = "登录接口")
    public ApiResult login (@Validated @RequestBody LoginDto loginDto) {
        try {
            UserEntity user = new UserEntity();
            //创建Token
            String token = JwtUtils.createToken(loginDto.getUsername(), loginDto.getPassword(), secret, 60 * 60);
            SecurityUtils.getSubject().login(JwtToken.build(user, token));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ApiResult.ok("登录成功").setData("超级管理员");
    }
}
