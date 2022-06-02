package com.learning.commons.module.controller;

import com.learning.commons.bean.JwtToken;
import com.learning.commons.entity.ApiResult;
import com.learning.commons.module.dto.LoginDto;
import com.learning.commons.module.entity.UserEntity;
import com.learning.commons.module.service.TestService;
import com.learning.commons.utils.JwtUtils;
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
            SecurityUtils.getSubject().login(new JwtToken(token));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ApiResult.ok("登录成功").setData("超级管理员");
    }
}
