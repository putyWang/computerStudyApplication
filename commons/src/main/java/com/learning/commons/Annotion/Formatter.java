package com.learning.commons.Annotion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Formatter {

    /**
     * 字典编码
     * @return
     */
    String dictCode() default "";

    String[] replace() default {};

    /**
     * 目标字段
     * @return
     */
    String targetField() default "";

    String key() default "";
}