package com.learning.commons.Annotion;

import java.lang.annotation.*;

/**
 * 权限控制类注解
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {

    /**
     * 权限控制值
     * @return
     */
    String value() default "";
}
