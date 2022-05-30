package com.learning.web.module.dto;

import com.learning.web.dto.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户信息返回类")
public class UserDto extends BaseDto {

    @ApiModelProperty(value = "用户id")
    private Long id;

    @ApiModelProperty(value = "用户名字")
    private String username;

    /**
     * 角色编码
     */
    @ApiModelProperty(value = "角色编码")
    private String RoleCode;
}
