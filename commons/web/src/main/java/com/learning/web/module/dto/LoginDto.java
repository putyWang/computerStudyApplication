package com.learning.web.module.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(value = "登录接收类", description = "登录接收类")
public class LoginDto {

    @ApiModelProperty(value = "用户名", notes = "用户名", required = true)
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", notes = "密码", required = true)
    @NotEmpty(message = "密码不能为空")
    private String password;
}
