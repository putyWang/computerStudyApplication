package com.learning.commons.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.commons.module.dao.UserMapper;
import com.learning.commons.module.entity.UserEntity;
import com.learning.commons.module.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {
}
