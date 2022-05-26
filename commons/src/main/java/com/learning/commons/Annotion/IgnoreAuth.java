package com.learning.commons.Annotion;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {
    boolean ignoreAuth() default true;

    boolean ignoreLogin() default true;
}
