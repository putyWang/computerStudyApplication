package com.learning.web.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.learning.web.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class RoleEntity
        extends BaseEntity {

    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Long id;

    private String name;

    private String roleCode;
}
