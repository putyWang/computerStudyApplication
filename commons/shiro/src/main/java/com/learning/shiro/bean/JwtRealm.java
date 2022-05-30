package com.learning.shiro.bean;

import com.google.common.collect.Sets;
import com.learning.core.bean.JwtToken;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

@Slf4j
@Component
public class JwtRealm
        extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //表示此Realm只支持JwtToken类型
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权（验证权限时调用）
     * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        // 根据用户名查找角色，请根据需求实现
        UserEntity user = (UserEntity)principalCollection.getPrimaryPrincipal();

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        // 添加相关用户角色
        authorizationInfo.setRoles(Sets.newHashSet(user.getRoleCode()));

        // 添加相关用户权限
        authorizationInfo.setStringPermissions(Sets.newHashSet(user.getPermission()));

        return authorizationInfo;
    }

    /**
     * 登陆时调用
     * */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        JwtToken jwtToken = (JwtToken) authenticationToken;

        // 获取token
        String token = jwtToken.getToken();

        // 获取用户
        UserEntity user = jwtToken.getPrincipal();

        // 用户不存在
        if (user == null) {
            throw new UnknownAccountException();
        }

        // 用户被禁用
        if(user.getStatus()==0){
            throw new LockedAccountException();
        }

        try {
            return new SimpleAuthenticationInfo(
                    user,
                    token,
                    getName()
            );
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        return "111";
    }

    /**
     * SimpleByteSource未实现Serializable接口，在缓存序列化时会报错，所以使用子类代替
     */
    public static class ShiroByteSource
            extends SimpleByteSource
            implements Serializable {
        /**
         * 父类没有无参构造器，这里加一个
         */
        public ShiroByteSource() {
            super(new byte[]{});
        }

        public ShiroByteSource(byte[] bytes) {
            super(bytes);
        }

        public ShiroByteSource(char[] chars) {
            super(chars);
        }

        public ShiroByteSource(String string) {
            super(string);
        }

        public ShiroByteSource(ByteSource source) {
            super(source);
        }

        public ShiroByteSource(File file) {
            super(file);
        }

        public ShiroByteSource(InputStream stream) {
            super(stream);
        }
    }
}
