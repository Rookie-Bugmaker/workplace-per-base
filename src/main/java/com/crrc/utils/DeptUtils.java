package com.crrc.utils;


import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.service.impl.CodeInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色相关工具类
 * @author mayiyao
 */
@Slf4j
@Component
public class DeptUtils {

    private static WkDataAPi wkDataApi;
    private static WkCoreAPi wkCoreApi;
    private static CodeInfoServiceImpl codeInfoService;

    @Autowired
    public void setWkCoreApi(WkCoreAPi wkCoreApi, CodeInfoServiceImpl codeInfoService, WkDataAPi wkDataApi) {
        DeptUtils.wkCoreApi = wkCoreApi;
        DeptUtils.codeInfoService = codeInfoService;
        DeptUtils.wkDataApi = wkDataApi;
    }

    public static Map<String, String> getDeptTranslateMap() {
        ResponseResultDTO<List<OrganizationDTO>> listResponseResultDTO = wkDataApi.allOrganization();
        if(listResponseResultDTO == null || ObjectUtils.isEmpty(listResponseResultDTO)) {
            throw new IllegalArgumentException("获取部门信息失败，请检查wkData服务的状态");
        }

        return listResponseResultDTO.getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName,
                (oldData, newData) -> oldData
        ));
    }

    /**
     * 获取所有部门（树型）
     * @return 部门名称
     */
    public static List<OrganizationTreeDTO> treeOrganization() {
        try {
            log.info("进入treeOrganization");
            // 先使用wkCoreApi翻译
            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataApi.treeOrganization();
            if(listResponseResultDTO == null || ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                log.error("treeOrganization部门查询失败，请检查wkDataApi状态，返回报文： {}", listResponseResultDTO);
                return Collections.emptyList();
            }
            return listResponseResultDTO.getData();
        } catch (Exception e) {
            log.error("treeOrganization出现内部错误", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有部门（列表）
     * @return 部门名称
     */
    public static List<OrganizationDTO> listOrganization() {
        try {
            log.info("进入listOrganization");
            // 先使用wkCoreApi翻译
            ResponseResultDTO<List<OrganizationDTO>> listResponseResultDTO = wkDataApi.allOrganization();
            if(listResponseResultDTO == null || ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                log.error("listOrganization部门查询失败，请检查wkDataApi状态，返回报文： {}", listResponseResultDTO);
                return Collections.emptyList();
            }
            return listResponseResultDTO.getData();
        } catch (Exception e) {
            log.error("listOrganization出现内部错误", e);
            return Collections.emptyList();
        }
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

    /**
     * 根据部门code获得公司code
     * @param deptCode 部门code
     * @return 公司code
     */
    public static Optional<String> getCompanyCodeByDeptCode(String deptCode) {
        ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataApi.treeOrganization();
        if (listResponseResultDTO == null || org.apache.commons.lang3.ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
            log.error("外部接口错误，获取树形组织信息失败");
            return Optional.empty();
        }
        List<OrganizationTreeDTO> data = listResponseResultDTO.getData();
        String parentCode = null;
        for(OrganizationTreeDTO root : data) {
            if (TreeUtils.containsBfs(root, n -> (deptCode != null) && deptCode.equals(n.getOrgCode()), OrganizationTreeDTO::getChildren)) {
                parentCode = root.getOrgCode();
                break;
            }
        }
        if (parentCode != null) {
            return Optional.of(parentCode);
        }
        return Optional.empty();
    }
}
