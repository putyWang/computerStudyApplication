package com.learning.web.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.web.module.dao.TestMapper;
import com.learning.web.module.entity.TestEntity;
import com.learning.web.module.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl
        extends ServiceImpl<TestMapper, TestEntity>
        implements TestService {
}
