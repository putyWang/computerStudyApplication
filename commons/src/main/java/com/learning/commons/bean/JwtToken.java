package com.learning.commons.bean;

import com.learning.commons.module.entity.UserEntity;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

@Data
public class JwtToken  implements AuthenticationToken {

    private UserEntity principal;

    private String token;

    @Override
    public UserEntity getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public static JwtToken build (UserEntity principal, String token) {
        JwtToken jwtToken = new JwtToken();
        jwtToken.setPrincipal(principal);
        jwtToken.setToken(token);
        return jwtToken;
    }
}
