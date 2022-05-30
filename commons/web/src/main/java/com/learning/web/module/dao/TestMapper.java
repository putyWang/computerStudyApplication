package com.learning.web.module.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.web.module.entity.TestEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper
        extends BaseMapper<TestEntity> {
}
