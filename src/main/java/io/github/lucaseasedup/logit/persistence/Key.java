package io.github.lucaseasedup.logit.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Key
{
    String name();
    String defaultValue() default "";
    
    /**
     * Tells what values are considered null.
     * 
     * @return KeyConstraint the key constraint.
     */
    KeyConstraint constraint() default KeyConstraint.NONE;
}
