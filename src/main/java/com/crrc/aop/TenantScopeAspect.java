package com.crrc.aop;

import com.crrc.annotation.TenantScope;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.utils.RoleUtils;
import com.crrc.utils.SecurityFrameworkUtils;
import com.crrc.utils.TreeUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TenantScopeAspect {

    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final WkDataAPi wkDataAPi;

    @Autowired
    public TenantScopeAspect(HrEmpBasicServiceImpl hrEmpBasicService, WkDataAPi wkDataAPi) {
        this.hrEmpBasicService = hrEmpBasicService;
        this.wkDataAPi = wkDataAPi;
    }


    @Around("within(@org.springframework.web.bind.annotation.RestController *) || " +
            "within(@org.springframework.stereotype.Controller *)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        TenantScope cfg = resolveTenantScope(pjp);

        boolean set1 = false;
        String oldTenantId = MultiTenantHandler.tenantId.get();

        if (cfg == null || RoleUtils.checkAnyMatch(cfg.filterRoleCode())) {
            try {
                MultiTenantHandler.tenantId.set("ignore");
                set1 = true;
                return pjp.proceed();
            } finally {
                if (set1) {
                    MultiTenantHandler.tenantId.set(oldTenantId); // 还原到上一个 tenant 或清理
                }
            }
        }
        String tenantId = generateTenantId(cfg.tenantLevel());

        if ((tenantId == null || tenantId.isEmpty())) {
            // 你也可以抛自定义异常，交给全局异常处理器返回统一错误码
            throw new IllegalArgumentException("Missing tenant_id");
        }

        boolean set2 = false;
        try {
            MultiTenantHandler.tenantId.set(tenantId);
            set2 = true;
            return pjp.proceed();
        } finally {
            if (set2) {
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

    private String generateTenantId(TenantScope.TenantLevel level) {
        try {
            // 查询当前登录人的所属部门
            String jobNo = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
            HrEmpBasic basic = hrEmpBasicService.getByUserCode(jobNo);

            if(TenantScope.TenantLevel.ORG.equals(level)) {
                String orgCode = "438";
                for (OrganizationTreeDTO datum : wkDataAPi.treeOrganization().getData()) {
                    if(
                            TreeUtils.containsBfs(
                                    datum,
                                    e -> basic.getDept().equals(e.getOrgCode()),
                                    OrganizationTreeDTO::getChildren)
                    ) {
                        orgCode = datum.getOrgCode();
                        break;
                    }
                }

                return orgCode;
            }

            if(TenantScope.TenantLevel.DEPT.equals(level)) {
                return basic.getDept();
            }

        } catch (Exception e) {
            log.error("tenant_id查询错误", e);
        }

        return "438";
    }
}
