package com.learning.web.module.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(value = "登录接收类", description = "登录接收类")
public class LoginDto {

    @ApiModelProperty(name = "用户名", notes = "用户名", required = true)
    @NotEmpty
    private String username;

    @ApiModelProperty(name = "密码", notes = "密码", required = true)
    @NotEmpty
    private String password;
}
