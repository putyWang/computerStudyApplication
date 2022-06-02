package com.learning.web.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.core.utils.MD5Utils;
import com.learning.core.utils.ObjectUtils;
import com.learning.shiro.bean.JwtToken;
import com.learning.shiro.contants.JwtConstants;
import com.learning.shiro.exception.SysLoginException;
import com.learning.shiro.utils.JwtUtils;
import com.learning.web.module.dao.UserMapper;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {

    /**
     * Token密钥
     */
    @Resource
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginDto login) {

        String username = login.getUsername();

        UserDto user = this.getBaseMapper().selectByUsername(username);
        Map<String, Object> claims = new HashMap<>();
        String token = "";

        //校验用户是否存在
        if (ObjectUtils.isEmpty(user)) {
            throw new UnknownAccountException("账户名有误");
        }

        //校验密码
        if (! user.getPassword().equals(MD5Utils.encrypt(login.getPassword()))) {
            throw new UnknownAccountException("密码有误");
        }

        //将账号相关信息加入到claims之中
        claims.put(JwtConstants.USER_NAME, username);
        claims.put(JwtConstants.ROLE_NAME, user.getRoleCode());
        claims.put(JwtConstants.PERMISSION_NAME, user.getPermissions());

        try {
            //创建Token
            token = jwtUtils.createToken(username, user.getId().toString(), claims);
            SecurityUtils.getSubject().login(new JwtToken(token));
        } catch (UnsupportedEncodingException e) {
            throw new UnknownAccountException("token转换有误");
        }

        return token;
    }
}
