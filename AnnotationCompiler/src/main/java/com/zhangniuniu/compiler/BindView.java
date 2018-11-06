package com.zhangniuniu.compiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author：zhangyong
 * @email：zhangyonglncn@gmail.com
 * @create_time: 05/11/2018 15:16
 * @description：
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value() default -1;
}
