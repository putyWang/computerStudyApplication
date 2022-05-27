package com.learning.commons.module.realm;

import com.learning.commons.bean.JwtToken;
import com.learning.commons.module.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;

@Slf4j
@Component
public class LoginRealm
        extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * //授权（验证权限时调用）
     * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        principalCollection.getPrimaryPrincipal();
        System.out.println("MyRealm doGetAuthorizationInfo() 方法授权 ");
        String token = principalCollection.toString();
        String username = JwtUtils.getClaim(token,"username");
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationException("token认证失败");
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        HashSet<String> roles = new HashSet<>();
        roles.add("超级管理员");
        info.setRoles(roles);
        info.setStringPermissions(roles);
        return info;

        //查询当前
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //其实这里应该是查询当前用户的角色或者权限去的,意思就是将当前用户设置一个svip和vip角色
        //权限设置一级权限和耳机权限 正常来说应该是去读取数据库只添加当前用户的角色权限的
        info.addRole("vip");
        info.addRole("svip");
        info.addStringPermission("一级权限");
        info.addStringPermission("二级权限");
        System.out.println("方法结束咯-------》》》");

        return info;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证, 如果没有权限注解的话就不会去走上面的方法只会走这个方法
     * 其实就是 过滤器传过来的token 然后进行 验证 authenticationToken.toString() 获取的就是
     * 你的token字符串,然后你在里面做逻辑验证就好了,没通过的话直接抛出异常就可以了
     * */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        System.out.println("认证-----------》》》》");
        System.out.println("1.toString ------>>> " + authenticationToken.toString());
        System.out.println("2.getCredentials ------>>> " + authenticationToken.getCredentials().toString());
        System.out.println("3. -------------》》 " +authenticationToken.getPrincipal().toString());
        String jwt = (String) authenticationToken.getCredentials();


        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();

        if (username == null) {
            return null;
        }
        String password = "";

        return new SimpleAuthenticationInfo(username, password,
                new ShiroByteSource("111"), getName());

//        if (!JwtUtils.verify(jwt)) {
//            throw new AuthenticationException("Token认证失败");
//        }

        return new SimpleAuthenticationInfo(jwt, jwt, "MyRealm");
    }


    public LoginRealm() {
        this.setCachingEnabled(true);
        this.setAuthenticationCachingEnabled(true);
        this.setAuthorizationCachingEnabled(true);
        this.setAuthenticationCacheName("shiro-authentication-cache");
        this.setAuthorizationCacheName("shiro-authorization-cache");

        // 密码比对器 SHA-256
        HashedCredentialsMatcher hashMatcher = new HashedCredentialsMatcher();
        hashMatcher.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
        hashMatcher.setStoredCredentialsHexEncoded(false);
        hashMatcher.setHashIterations(1024);
        this.setCredentialsMatcher(hashMatcher);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws UnauthorizedException {

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
