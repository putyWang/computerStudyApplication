package com.learning.web.module.dto;

import com.learning.web.dto.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
