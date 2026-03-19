package com.crrc.aop;

import com.crrc.handler.MultiTenantHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantIgnoreAspect {
    @Pointcut("execution(public * com.crrc.service..*(..)) || " +
            "execution(public * com.crrc.controller..*(..))")
    public void crrcPublicMethod() {}

    @Pointcut("@annotation(com.crrc.annotation.TenantIgnore)")
    public void methodAnnotated() {}

    @Pointcut("@within(com.crrc.annotation.TenantIgnore)")
    public void typeAnnotated() {}

    @Pointcut("@target(com.crrc.annotation.TenantIgnore)")
    public void classAnnotated() {}

    @Around("methodAnnotated() || typeAnnotated() || (classAnnotated() && crrcPublicMethod())")
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