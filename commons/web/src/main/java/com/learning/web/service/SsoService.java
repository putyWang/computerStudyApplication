package com.learning.web.service;

import com.learning.web.module.dto.LoginDto;

public interface SsoService{

    void login(LoginDto loginDto);
}
