package com.learning.shiro.bean;

import com.google.common.collect.Sets;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.ObjectUtils;
import com.learning.shiro.contants.JwtConstants;
import com.learning.shiro.exception.NotLoginException;
import com.learning.shiro.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.learning.shiro.exception.NotLoginException.*;

@Slf4j
@Component
public class JwtRealm
        extends AuthorizingRealm {

    @Resource
    private JwtUtils jwtUtils;

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
        String token = (String)principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Map<String, Object> claim = jwtUtils.getClaim(token);

        if (CollectionUtils.isEmpty(claim)) {
            throw NotLoginException.newInstance(INVALID_TOKEN, INVALID_TOKEN, token);
        }

        // 添加相关用户角色
        Object role = claim.get(JwtConstants.ROLE_NAME);

        if (role instanceof String && ! ObjectUtils.isEmpty(role))
            authorizationInfo.setRoles(Sets.newHashSet((String)role));
        else
            throw new AuthenticationException("角色添加有误");

        // 添加相关用户权限
        Object permission = claim.get(JwtConstants.PERMISSION_NAME);

        if (permission instanceof List && ObjectUtils.isEmpty(permission))
            authorizationInfo.setStringPermissions(Sets.newHashSet((List<String>)permission));
        else
            throw new AuthenticationException("权限添加有误");

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

        if (ObjectUtils.isEmpty(token)) {
            throw NotLoginException.newInstance(NOT_TOKEN, NOT_TOKEN);
        }

        Map<String, Object> claim = jwtUtils.getClaim(token);

        if (CollectionUtils.isEmpty(claim)) {
            throw NotLoginException.newInstance(INVALID_TOKEN, INVALID_TOKEN, token);
        }

        Object username = claim.get(JwtConstants.USER_NAME);

        // 用户验证有误
        if (ObjectUtils.isEmpty(username) || !(username instanceof String))
            throw new UnknownAccountException("用户名有误");

        Object status = claim.get(JwtConstants.USER_STATUS);

        // 用户状态验证有误
        if(! (status instanceof Integer) || ! ObjectUtils.isEmpty(status))
            throw new UnknownAccountException("用户状态输入有误");

        // 用户被禁用
        if(status.equals(0)){
            throw new LockedAccountException("用户已被禁用");
        }

        //将相关内容保存到SimplePrincipalCollection中
        try {
            return new SimpleAuthenticationInfo(
                    claim,
                    token,
                    (String)username
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
