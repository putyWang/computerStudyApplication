package com.learning.core.annotion;

import java.lang.annotation.*;

/**
 * 前端不需要的数据
 */
@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoSkip {
}
