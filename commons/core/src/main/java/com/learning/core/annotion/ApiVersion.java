package com.learning.core.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ApiVersion {
    ApiVersion.Group[] group() default {ApiVersion.Group.DEFAULT};

    String[] value() default {};

    public static enum Group {
        DEFAULT("default"),
        COMMON("基础接口"),
        SYSYTEM("系统模块"),
        AUTH("权限模块");

        private final String display;

        private Group(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return this.display;
        }
    }
}
