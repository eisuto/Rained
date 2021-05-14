package com.rained.annotation;

import java.lang.annotation.*;

/**
 * @author eisuto
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ingredient {

    /**
     * 要注入的类的类型
     */
    Class<?> value() default Class.class;

    /**
     * @return	Bean的名称
     */
    String name() default "";

}
