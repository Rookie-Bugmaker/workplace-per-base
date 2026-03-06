package com.crrc.aop;

import com.crrc.handler.MultiTenantHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantIgnoreAspect {
    @Around("@annotation(com.crrc.annotation.TenantIgnore)")
    public Object handleTenantIgnore(ProceedingJoinPoint joinPoint) throws Throwable {
        String oldTenantId = MultiTenantHandler.tenantId.get();
        Object var;
        try {

            MultiTenantHandler.tenantId.set("ignore");
            var = joinPoint.proceed();
        } finally {
            MultiTenantHandler.tenantId.set(oldTenantId);
        }

        return var;
    }
}