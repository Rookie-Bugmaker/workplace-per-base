package com.crrc.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantScope {
     String[] filterRoleCode() default {};

     TenantLevel tenantLevel();

     enum TenantLevel {
         DEPT, ORG
     }
}
