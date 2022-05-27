package com.learning.commons.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.commons.module.dao.TestMapper;
import com.learning.commons.module.entity.TestEntity;
import com.learning.commons.module.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl
        extends ServiceImpl<TestMapper, TestEntity>
        implements TestService {
}
