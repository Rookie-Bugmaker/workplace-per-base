package com.crrc.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecryptBody {
    AadMode aadMode() default AadMode.NONE;

    String aadHeader() default "X-AAD";

    enum AadMode {
        NONE,
        URI,
        HEADER
    }
}
