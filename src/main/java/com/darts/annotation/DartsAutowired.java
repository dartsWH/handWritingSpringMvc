package com.darts.annotation;

import java.lang.annotation.*;

/**
 * 手写autowired注解
 * @author dartsWH
 * @Date 20190101
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DartsAutowired {
    String value() default "";
}
