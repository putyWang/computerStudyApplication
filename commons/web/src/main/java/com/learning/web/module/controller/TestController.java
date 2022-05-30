package com.learning.web.module.controller;

import com.learning.core.annotion.Model;
import com.learning.web.controller.BaseController;
import com.learning.web.module.dto.TestDto;
import com.learning.web.module.entity.TestEntity;
import com.learning.web.module.service.TestService;
import com.learning.web.service.BaseService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Model("test")
@Api(value = "test")
public class TestController
        extends BaseController<TestEntity, TestDto> {

    @Autowired
    private TestService testService;

    @Override
    public BaseService<TestDto, TestEntity> getService() {
        return testService;
    }

    @Override
    public TestDto getDto() {
        return new TestDto();
    }

    @Override
    public TestEntity getEntity() {
        return new TestEntity();
    }
}
