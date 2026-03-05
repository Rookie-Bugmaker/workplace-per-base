package com.crrc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要提取的表格列
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WordTemplateRequire {
    /**
     * value用于指示placeHolder
     */
    String value() default "";

    int index() default Integer.MAX_VALUE;
}
