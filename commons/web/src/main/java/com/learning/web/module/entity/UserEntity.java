package com.learning.web.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.learning.core.annotion.Unique;
import com.learning.web.entity.BaseEntity;
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

    @Unique
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

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 身份证号码
     */
    private String idCard;
}
