package com.learaning.demo.Annotion;

import com.learaning.demo.enums.QueryEnum;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    /**
     * 字段匹配方式，默认为eq
     * @return
     */
    QueryEnum value() default QueryEnum.EQ;

    /**
     * 注解的位置
     * @return
     */
    boolean where() default true;

    /**
     * 表字段名
     * @return
     */
    String column() default "";;
}
