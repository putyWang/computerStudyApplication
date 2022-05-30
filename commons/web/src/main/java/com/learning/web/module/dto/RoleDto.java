package com.learning.web.module.dto;

import com.learning.web.dto.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class RoleDto
        extends BaseDto {

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称", notes = "角色名称", required = true)
    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @ApiModelProperty(value = "角色编码", notes = "角色编码", required = true)
    @NotEmpty(message = "角色编码不能为空")
    private String roleCode;
}
