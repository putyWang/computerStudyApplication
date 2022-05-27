package com.learning.commons.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.learning.commons.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
