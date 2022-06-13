package com.learning.web.module.controller;

import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson.JSON;
import com.learning.core.bean.SuccessRegistryMessage;
import com.learning.core.cache.RedisCache;
import com.learning.core.constants.KafkaConstants;
import com.learning.core.utils.CommonBeanUtil;
import com.learning.core.utils.MD5Utils;
import com.learning.core.bean.ApiResult;
import com.learning.core.utils.ObjectUtils;
import com.learning.core.utils.StringUtils;
import com.learning.exception.exception.VerificationCodeException;
import com.learning.exception.exception.verificationCodeErrorException;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/sso")
@Api(value = "单点登录相关类")
public class SsoController {

    private static final Logger log = LoggerFactory.getLogger(SsoController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/login")
    @ApiOperation(value = "登录接口")
    public ApiResult login (@Validated LoginDto loginDto) {

        return ApiResult.ok(userService.login(loginDto));
    }

    @PutMapping("/registered")
    @ApiOperation(value = "注册接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "verificationCode", value = "验证码", required = true),
            @ApiImplicitParam(name = "verificationUUID", value = "验证码缓存key", required = true)
    })
    public ApiResult registered (
            @RequestBody @Validated UserDto userDto,
            @RequestParam @NotEmpty(message = "验证码不能为空") String verificationCode,
            @RequestParam @NotEmpty(message = "验证码缓存key不能为空") String verificationUUID
    ) {
        //验证验证码是否正确
        Object o = redisCache.get(verificationUUID);
        if (ObjectUtils.isEmpty(o)) {
            throw new verificationCodeErrorException("验证码已过期，请重新获取");
        }
        if (! o.equals(verificationCode)) {
            throw new verificationCodeErrorException("验证码有误，请重新输入");
        }
        //删除验证码缓存
        redisCache.delete(verificationUUID);

        //验证身份证号码合法性
        if (! IdcardUtil.isValidCard(userDto.getIdCard())) {
            throw new VerificationCodeException("身份证号码格式有误");
        }

        //存储用户信息
        UserEntity user = new UserEntity();
        UserEntity userEntity = CommonBeanUtil.copyAndFormat(user, userDto);
        String password = MD5Utils.encrypt(userDto.getPassword());
        userEntity.setPassword(password);
        userService.insert(userEntity);

        //生产注册成功消息
        SuccessRegistryMessage successRegistryMessage = new SuccessRegistryMessage();
        successRegistryMessage.setId(userEntity.getId());
        successRegistryMessage.setEmailAddress(userEntity.getEmail());
        successRegistryMessage.setUsername(userEntity.getUsername());
        log.info(userEntity.getUsername() + "注册成功消息发送成功");
        kafkaTemplate.send(KafkaConstants.REGISTRY_SUCCESS_KEY, JSON.toJSONString(successRegistryMessage));

        return ApiResult.ok("注册成功");
    }
}
