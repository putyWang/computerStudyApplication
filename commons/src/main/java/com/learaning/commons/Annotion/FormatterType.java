package com.learaning.commons.Annotion;

import com.learaning.commons.enums.FormatterEnum;

import java.lang.annotation.*;

/**
 * 非基础类型数据
 */
@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface FormatterType {

    FormatterEnum type() default FormatterEnum.OBJECT;
}
