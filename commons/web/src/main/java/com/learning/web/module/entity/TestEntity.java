package com.learning.web.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.learning.web.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_table")
public class TestEntity
        extends BaseEntity
        implements Serializable {

    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Long id;

    private String name;
}
