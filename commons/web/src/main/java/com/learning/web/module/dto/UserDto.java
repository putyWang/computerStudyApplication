package com.learning.web.module.dto;

import com.learning.core.annotion.MobilePhoneNumber;
import com.learning.web.dto.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户信息返回类")
public class UserDto extends BaseDto {

    @ApiModelProperty(value = "用户id", hidden = true)
    private Long id;

    @ApiModelProperty(value = "用户名字", notes = "用户名字", required = true)
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "用户密码", notes = "用户密码", required = true)
    @NotEmpty(message = "用户密码不能为空")
    private String password;

    @ApiModelProperty(value = "用户状态", notes = "默认为1", example = "1为正常，0为被锁定")
    private Integer status;

    /**
     * 角色编码
     */
    @ApiModelProperty(value = "角色编码", notes = "角色编码", required = true)
    @NotEmpty(message = "角色编码不能为空")
    private String roleCode;

    @ApiModelProperty(value = "用户权限信息", hidden = true)
    private List<String> permissions;

    /**
     * 邮箱地址
     * 正则表达式（"/^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$/"）
     */
    @ApiModelProperty(value = "邮箱地址", notes = "邮箱地址", required = true)
    @NotEmpty(message = "邮箱地址不能为空")
    @Email(regexp = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", message = "邮箱地址格式有误，请重新输入")
    private String email;

    @ApiModelProperty(value = "电话号码", notes = "电话号码", required = true)
    @MobilePhoneNumber(message = "电话号码格式有误，请重新输入")
    private String phoneNumber;

    /**
     * 身份证号
     */
    @ApiModelProperty(value = "身份证号", notes = "身份证号", required = true)
    @NotEmpty(message = "身份证号不能为空")
    private String idCard;
}
