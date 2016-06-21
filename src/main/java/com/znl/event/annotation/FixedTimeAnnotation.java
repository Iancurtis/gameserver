package com.znl.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时执行注解
 * Created by pwy on 2016/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedTimeAnnotation {
    String description() default "";//描述
    int []actionTimes() default {};//触发时间点，具体与quartz_jobs.xml对应
}
