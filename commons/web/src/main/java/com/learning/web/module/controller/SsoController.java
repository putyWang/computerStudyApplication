package com.learning.web.module.controller;

import com.learning.core.utils.ObjectUtils;
import com.learning.shiro.bean.JwtToken;
import com.learning.core.bean.ApiResult;
import com.learning.shiro.contants.JwtConstants;
import com.learning.shiro.utils.JwtUtils;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.TestService;
import com.learning.web.module.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sso")
@Api(value = "单点登录相关类")
public class SsoController {

    private TestService testService;

    @Autowired
    private UserService userService;

    @Autowired
    public void setTestService(TestService testService) {
        this.testService = testService;
    }

    /**
     * Token密钥
     */
    @Resource
    private JwtUtils jwtUtils;


    @GetMapping("/login")
    @ApiOperation(value = "登录接口")
    public ApiResult login (@Validated LoginDto loginDto) {

        UserDto user = userService.login(loginDto);
        Map<String, Object> claims = new HashMap<>();
        String token = "";

        try {
            //校验用户是否存在
            if (ObjectUtils.isEmpty(user)) {
                throw new UnknownAccountException("未知账户");
            }
            //将账号相关信息加入到claims之中
            String username = user.getUsername();
            claims.put(JwtConstants.USER_NAME, username);
            claims.put(JwtConstants.ROLE_NAME, user.getRoleCode());
            claims.put(JwtConstants.PERMISSION_NAME, user.getPermissions());
            //创建Token
            token = jwtUtils.createToken(loginDto.getUsername(), user.getId().toString(), claims);
            SecurityUtils.getSubject().login(new JwtToken(token));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ApiResult.ok(token);
    }
}
