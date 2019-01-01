package com.darts.annotation;

import java.lang.annotation.*;

/**
 * 自定义service注解
 * @author dartsWH
 * @date 20190101
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DartsService {
    String value() default "";
}
