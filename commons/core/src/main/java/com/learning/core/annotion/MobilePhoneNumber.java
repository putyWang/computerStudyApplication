package com.learning.core.annotion;

import com.learning.core.constraint.MobilePhoneNumberValidator;

import javax.validation.Constraint;
import java.lang.annotation.*;

/**
 * 电话号码合法性验证注解
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MobilePhoneNumberValidator.class)
public @interface MobilePhoneNumber {

    /**
     * 错误信息
     * @return
     */
    String message() default "";

    /**
     * 电话号码正则表达式
     * @return
     */
    String regex() default "/^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$/";

    /**
     * 手机号码位数
     * @return
     */
    int size() default 11;
}
