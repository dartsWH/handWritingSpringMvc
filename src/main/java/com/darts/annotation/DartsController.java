package com.darts.annotation;

import java.lang.annotation.*;

/**
 * 手写controller注解
 * @date 20190101
 * @author dartsWH
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DartsController {
    String value() default "";
}
