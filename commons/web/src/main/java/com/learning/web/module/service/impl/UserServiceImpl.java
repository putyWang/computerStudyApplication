package com.learning.web.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.web.module.dao.UserMapper;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {
}
