package com.learning.commons.module.controller;

import com.learning.commons.Annotion.Model;
import com.learning.commons.Annotion.Permission;
import com.learning.commons.controller.BaseController;
import com.learning.commons.entity.ApiResult;
import com.learning.commons.module.dto.TestDto;
import com.learning.commons.module.entity.TestEntity;
import com.learning.commons.module.service.TestService;
import com.learning.commons.service.BaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
