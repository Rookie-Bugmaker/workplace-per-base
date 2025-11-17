package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.CompanyDTO;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.DictItemReqDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.*;
import com.crrc.utils.DictTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 获取审批人信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Slf4j
@RestController
@RequestMapping("/empFlowable")
@RefreshScope
@Tag(name = "获取审批人信息", description = "获取审批人信息")
public class HrEmpFlowableController {

    @Resource
    private SysServerClient sysServerClient;
    @Autowired
    private DictTypeUtils dictTypeUtils;
    @Autowired
    private WkDataAPi wkDataAPi;


    @GetMapping("/findUserByDeptIdAndRoleCode")
    @Operation(summary = "HrEmpFlowableController工作流获取审批人信息")
    @AuditLog(value = "HrEmpFlowableController工作流获取审批人信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<SysUserDTO>> findUserByDeptIdAndRoleCode(Long companyId, Long deptId, String roleCode) {
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode 查询参数companyId:{},deptId:{},roleCode:{}", companyId, deptId, roleCode);
        if (ObjectUtils.isEmpty(deptId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "deptId不能为空");
        }
        if (ObjectUtils.isEmpty(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }
        if (ObjectUtils.isEmpty(companyId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "companyId不能为空");
        }
        // 查询人员信息
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCode(roleCode);
        sysUserRequestDTO.setCompanyId(companyId);
        sysUserRequestDTO.setDeptId(deptId);
        ResultData<List<SysUserDTO>> userByRoleCodeAndCompanyId = sysServerClient.findUserByRoleCode(sysUserRequestDTO);
        List<SysUserDTO> result = new ArrayList<>();
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode {}", userByRoleCodeAndCompanyId.getResult());
        if(!ObjectUtils.isEmpty(userByRoleCodeAndCompanyId) && !ObjectUtils.isEmpty(userByRoleCodeAndCompanyId.getResult())){
            result.addAll(userByRoleCodeAndCompanyId.getResult());
        }
        // 查询字典中配置的审批人
        log.info("开始查询字典审批人");
        List<DictItemReqDTO> humanResourceAuditConfig = dictTypeUtils.getDict("human_resource_audit_config");
        Map<String, List<String>> code2AuditList = humanResourceAuditConfig.stream().collect(Collectors.groupingBy(
                DictItemReqDTO::getItemCode,
                Collectors.mapping(
                        DictItemReqDTO::getItemName,
                        Collectors.toList()
                )
        ));

        log.info("字典审批人查询完毕: size: {} {}", humanResourceAuditConfig.size(), humanResourceAuditConfig);

        // 根据deptId查orgCode
        ResultData<DeptDTO> pageByDeptId = sysServerClient.findDeptByDeptId(deptId);
        if(pageByDeptId == null || pageByDeptId.getResult() == null) {
            log.error("未查询到部门信息:{}", deptId);
        } else {
            DeptDTO deptDTO = pageByDeptId.getResult();

            log.info("部门信息查询完毕: {}", deptDTO.getOrgCode());

            if(code2AuditList.containsKey(deptDTO.getOrgCode())) {
                log.info("开始查询个人信息，组装审批人");
                List<String> userCodeList = code2AuditList.get(deptDTO.getOrgCode());
                for (String userCode : userCodeList) {
                    ResultData<SysDeptPersonDTO> byUserCode = sysServerClient.findByUserCode(userCode);
                    if (byUserCode == null || byUserCode.getResult() == null || byUserCode.getResult().getPersonId() == null) {
                        log.error("工号查询个人详细信息失败:{}", userCode);
                        continue;
                    }

                    Long personId = byUserCode.getResult().getPersonId();
                    ResultData<List<SysUserDTO>> byPersonIds = sysServerClient.findByPersonIds(Collections.singletonList(personId));
                    if (byPersonIds == null || ObjectUtils.isEmpty(byPersonIds.getResult())) {
                        log.error("人员id查询个人详细信息失败:{}", personId);
                        continue;
                    }

                    result.addAll(byPersonIds.getResult());
                    log.info("个人信息审批人: {}", byPersonIds.getResult().get(0).getUserName());
                }
            }
        }

        // 根据companyId查orgCode
        ResultData<PaginationGrid<CompanyDTO>> companyDTOResultData = sysServerClient.pageCompany(1, 10000);
        log.info("开始查询公司信息");
        if(companyDTOResultData == null || companyDTOResultData.getResult() == null || ObjectUtils.isEmpty(companyDTOResultData.getResult().getRows())) {
            log.error("未查询到公司信息:{}", companyId);
        } else if(companyDTOResultData.getResult().getRows().size() != companyDTOResultData.getResult().getTotal()) {
            log.error("公司数量异常，未能查全");
        } else {
            Optional<CompanyDTO> companyDTO = companyDTOResultData.getResult().getRows().stream().filter(e -> companyId.equals(e.getId())).findFirst();
            if(companyDTO.isPresent()) {
                String orgCode = companyDTO.get().getOrgCode();
                if(code2AuditList.containsKey(orgCode)) {
                    log.info("开始查询个人信息，组装审批人");
                    List<String> userCodeList = code2AuditList.get(orgCode);
                    for (String userCode : userCodeList) {
                        ResultData<SysDeptPersonDTO> byUserCode = sysServerClient.findByUserCode(userCode);
                        if (byUserCode == null || byUserCode.getResult() == null || byUserCode.getResult().getPersonId() == null) {
                            log.error("工号查询个人详细信息失败:{}", userCode);
                            continue;
                        }

                        Long personId = byUserCode.getResult().getPersonId();
                        ResultData<List<SysUserDTO>> byPersonIds = sysServerClient.findByPersonIds(Collections.singletonList(personId));
                        if (byPersonIds == null || ObjectUtils.isEmpty(byPersonIds.getResult())) {
                            log.error("人员id查询个人详细信息失败:{}", personId);
                            continue;
                        }

                        result.addAll(byPersonIds.getResult());
                        log.info("个人信息审批人: {}", byPersonIds.getResult().get(0).getUserName());
                    }
                }
            }
        }

        if(ObjectUtils.isEmpty(result)) {
            log.info("未查到审批人数据");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }

        return ResponseResult.success(result);
    }


    @Deprecated
    @GetMapping("/findUserByRoleCodeAndCompanyId")
    @Operation(summary = "findUserByRoleCodeAndCompanyId工作流获取审批人信息")
    @AuditLog(value = "findUserByRoleCodeAndCompanyId工作流获取审批人信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<SysUserDTO>> findUserByRoleCodeAndCompanyId(Long companyId, String roleCode) {
        log.info("触发人力资源获取人员信息接口findUserByRoleCodeAndCompanyId 查询参数companyId:{},roleCode:{}", companyId, roleCode);
        if (ObjectUtils.isEmpty(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }
        if (ObjectUtils.isEmpty(companyId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "companyId不能为空");
        }
        // 查询人员信息
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCode(roleCode);
        sysUserRequestDTO.setCompanyId(companyId);
        ResultData<List<SysUserDTO>> userByRoleCodeAndCompanyId = sysServerClient.findUserByRoleCodeAndCompanyId(sysUserRequestDTO);
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode {}", userByRoleCodeAndCompanyId.getResult());
        if(ObjectUtils.isEmpty(userByRoleCodeAndCompanyId) || ObjectUtils.isEmpty(userByRoleCodeAndCompanyId.getResult())){
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }
        List<SysUserDTO> result = userByRoleCodeAndCompanyId.getResult();
        return ResponseResult.success(result);
    }


}
