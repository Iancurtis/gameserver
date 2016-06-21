package com.znl.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 零点事件执行注解
 * Created by pwy on 2016/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ZeroTimeAnnotation {
    String description() default "";//描述
}
