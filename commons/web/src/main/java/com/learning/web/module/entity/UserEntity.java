package com.learning.web.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.learning.web.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserEntity extends BaseEntity {

    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Long id;

    private String username;

    private String password;

    /**
     * 角色编码
     */
    private String RoleCode;

    /**
     * 表示用户状态
     * 1为正常
     * 0为以禁用
     */
    private Integer status;

    private List<String> permission;
}
