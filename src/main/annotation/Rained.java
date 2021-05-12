package main.annotation;

import java.lang.annotation.*;

/**
 * @author eisuto
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Ingredient
public @interface Rained {

}
