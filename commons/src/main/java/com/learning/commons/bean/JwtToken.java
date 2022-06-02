package com.learning.commons.bean;

import com.learning.commons.module.entity.UserEntity;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

@Data
public class JwtToken  implements AuthenticationToken {

    // 使用构造函数传参，重写2个Toke的返回方法，实现替换shiro默认token为jwtToken
    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    }
}
