package main.annotation;

import java.lang.annotation.*;

/**
 * @author eisuto
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoCombine {

}
