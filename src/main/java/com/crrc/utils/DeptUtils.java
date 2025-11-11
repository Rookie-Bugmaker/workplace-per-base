package com.crrc.utils;


import com.crrc.api.WkCoreAPi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public void setWkCoreApi(WkCoreAPi wkCoreApi) {
        DeptUtils.wkCoreApi = wkCoreApi;
    }

    /**
     * 根据部门编码查询部门名称
     * @param code 部门编码
     * @return 部门名称
     */
    public static String getDeptNameByCode(String code) {
        try {
            log.info("进入查询，deptCode: {}", code);
            return wkCoreApi.getDeptByOrgCode(code).getDepartmentName();
        } catch (Exception e) {
            log.warn("部门名称查询失败, orgCode: {}", code, e);
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
            log.info("进入查询，deptCode: {}", code);
            return Optional.of(wkCoreApi.getDeptByOrgCode(code).getId());
        } catch (Exception e) {
            log.warn("部门名称查询失败, orgCode: {}", code, e);
            return Optional.empty();
        }
    }
}
