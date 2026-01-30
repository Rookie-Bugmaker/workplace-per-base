package com.crrc.advice;

import com.crrc.annotation.TenantScope;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.utils.TenantIdGenerateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order( Ordered.HIGHEST_PRECEDENCE + 10 )
public class TenantScopeAspect {

    private final TenantIdGenerateUtil tenantIdGenerateUtil;

    @Autowired
    public TenantScopeAspect(TenantIdGenerateUtil tenantIdGenerateUtil) {
        this.tenantIdGenerateUtil = tenantIdGenerateUtil;
    }


    @Around("within(@org.springframework.web.bind.annotation.RestController *) || " +
            "within(@org.springframework.stereotype.Controller *)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        TenantScope cfg = resolveTenantScope(pjp);
        if (cfg == null) {
            return pjp.proceed();
        }

        String tenantId = tenantIdGenerateUtil.generateTenantId();
        String oldTenantId = MultiTenantHandler.tenantId.get();

        if ((tenantId == null || tenantId.isEmpty())) {
            // 你也可以抛自定义异常，交给全局异常处理器返回统一错误码
            throw new IllegalArgumentException("Missing tenant_id");
        }

        boolean set = false;
        try {
            MultiTenantHandler.tenantId.set(tenantId);
            set = true;
            return pjp.proceed();
        } finally {
            if (set) {
                MultiTenantHandler.tenantId.set(oldTenantId); // 还原到上一个 tenant 或清理
            }
        }
    }

    private TenantScope resolveTenantScope(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        TenantScope onMethod = method.getAnnotation(TenantScope.class);
        if (onMethod != null) return onMethod;

        Class<?> targetClass = pjp.getTarget().getClass();
        return targetClass.getAnnotation(TenantScope.class);
    }
}
