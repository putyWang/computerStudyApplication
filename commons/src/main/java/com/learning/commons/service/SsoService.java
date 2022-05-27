package com.learning.commons.service;

import com.learning.commons.module.dto.LoginDto;

public interface SsoService{

    void login(LoginDto loginDto);
}
