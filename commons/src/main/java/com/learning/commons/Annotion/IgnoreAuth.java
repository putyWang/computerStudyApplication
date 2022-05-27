package com.learning.commons.Annotion;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {
    /**
     * 不需要鉴权
     * @return
     */
    boolean ignoreAuth() default true;

    /**
     * 不需登录权限
     * @return
     */
    boolean ignoreLogin() default true;
}
