package com.learning.web.module.service;

import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.service.BaseService;

public interface UserService
        extends BaseService<UserDto, UserEntity> {

    UserDto login(LoginDto login);
}
