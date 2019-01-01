package com.darts.annotation;

import com.sun.org.apache.regexp.internal.RE;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DartsRequestParam {
    String value() default "";
}
