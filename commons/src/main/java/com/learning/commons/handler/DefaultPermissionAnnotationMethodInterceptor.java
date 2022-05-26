package com.learning.commons.handler;

import com.learning.commons.Annotion.Model;
import com.learning.commons.Annotion.Permission;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;

import java.lang.annotation.Annotation;

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

        try {
            //因为需要方法所在的类，就直接在拦截器处理了授权认证了
            //自定义注解授权处理逻辑
            Annotation typeAnnotation = getAnnotation(mi);

            if (!(typeAnnotation instanceof Permission)) return;

            Permission permission = (Permission) typeAnnotation;
            //获取Controller注解model
            Class<?> declaringClass = mi.getMethod().getDeclaringClass();
            Model annotation= declaringClass.getAnnotation(Model.class);

            if(annotation!=null){
                String method=mi.getMethod().getName();
                String base= annotation.value();
                ((DefaultPermissionAnnotationHandler)getHandler()).assertAuthorized(base+ "_" + permission.value());
            }
        }catch(AuthorizationException ae) {
            if (ae.getCause() == null) ae.initCause(new AuthorizationException("Not authorized to invoke method: " + mi.getMethod()));
            throw ae;
        }
    }
}
