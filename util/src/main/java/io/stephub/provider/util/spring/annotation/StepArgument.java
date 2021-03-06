package io.stephub.provider.util.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface StepArgument {
    String name();

    boolean strict() default false;

    StepDoc doc() default @StepDoc();
}
