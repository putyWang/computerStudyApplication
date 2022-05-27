package com.learning.commons.handler;

import com.learning.commons.Annotion.IgnoreAuth;
import com.learning.commons.Annotion.Model;
import com.learning.commons.Annotion.Permission;
import com.learning.commons.exception.ExceptionBuilder;
import com.learning.commons.exception.NotLoginException;
import com.learning.commons.utils.ObjectUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 自定义注解授权拦截器
 */
public class DefaultPermissionAnnotationMethodInterceptor
        extends AuthorizingAnnotationMethodInterceptor {

    public DefaultPermissionAnnotationMethodInterceptor() {
        //注入自定义注解授权处理器
        super(new DefaultPermissionAnnotationHandler());
    }

    public void assertAuthorized(MethodInvocation mi)
            throws AuthorizationException {

        Method requestMethod = mi.getMethod();
        try {
            IgnoreAuth ignoreAuth = requestMethod.getAnnotation(IgnoreAuth.class);
            //忽略登录则直接返回
            if (! ObjectUtils.isEmpty(ignoreAuth) && ignoreAuth.ignoreLogin()) {
                    return;
            }

            //用户未登录则直接跳转登录页面
            if (! getSubject().isAuthenticated())
                throw NotLoginException.newInstance("未登录","0");

            //ignoreAuth为真时 忽略权限验证
            if (! ObjectUtils.isEmpty(ignoreAuth) && ignoreAuth.ignoreAuth()) {
                return;
            }

            //因为需要方法所在的类，就直接在拦截器处理了授权认证了
            //自定义注解授权处理逻辑
            Annotation typeAnnotation = getAnnotation(mi);

            if (!(typeAnnotation instanceof Permission)) return;

            Permission permission = (Permission) typeAnnotation;
            //获取Controller注解model
            Class<?> declaringClass = requestMethod.getDeclaringClass();
            Model annotation= declaringClass.getAnnotation(Model.class);

            //若类上没有Model注解，则表明无法访问
            if(annotation!=null){
                String method= requestMethod.getName();
                String base= annotation.value();
                ((DefaultPermissionAnnotationHandler)getHandler()).assertAuthorized(base+ "_" + permission.value());
            } else
                throw new AuthorizationException("Not authorized to invoke method: " + requestMethod);
        }catch(AuthorizationException ae) {
            if (ae.getCause() == null) ae.initCause(new AuthorizationException("Not authorized to invoke method: " + requestMethod));
            throw ae;
        }
    }
}
