package com.crrc.utils;


import com.crrc.api.WkCoreAPi;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.service.impl.CodeInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * 角色相关工具类
 * @author mayiyao
 */
@Slf4j
@Component
public class DeptUtils {

    private static WkCoreAPi wkCoreApi;
    private static CodeInfoServiceImpl codeInfoService;

    @Autowired
    public void setWkCoreApi(WkCoreAPi wkCoreApi, CodeInfoServiceImpl codeInfoService) {
        DeptUtils.wkCoreApi = wkCoreApi;
        DeptUtils.codeInfoService = codeInfoService;
    }

    /**
     * 根据部门编码查询部门名称
     * @param code 部门编码
     * @return 部门名称
     */
    public static String getDeptNameByCode(String code) {
        try {
            log.info("进入getDeptNameByCode查询，deptCode: {}", code);
            // 先使用wkCoreApi翻译
            DeptDTO deptByOrgCode = wkCoreApi.getDeptByOrgCode(code);
            if(deptByOrgCode != null && StringUtils.hasText(deptByOrgCode.getDepartmentName())) {
                return deptByOrgCode.getDepartmentName();
            }

            // 如果翻译失败，尝试codeInfoService，translateCode失败会返回code
            return codeInfoService.translateCode(code, "hr_emp_appointment", "APPOINTMENT_ORG");
        } catch (Exception e) {
            log.warn("getDeptNameByCode部门名称查询失败, orgCode: {}", code, e);
            return code;
        }
    }

    /**
     * 根据部门编码查询部门iD
     * @param code 部门编码
     * @return 部门ID
     */
    public static Optional<Long> getDeptIdByCode(String code) {
        try {
            log.info("进入getDeptIdByCode查询，deptCode: {}", code);
            return Optional.of(wkCoreApi.getDeptByOrgCode(code).getId());
        } catch (Exception e) {
            log.warn("getDeptIdByCode部门名称查询失败, orgCode: {}", code, e);
            return Optional.empty();
        }
    }
}
