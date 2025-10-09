package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;



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
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode {}", userByRoleCodeAndCompanyId.getResult());
        if(ObjectUtils.isEmpty(userByRoleCodeAndCompanyId) || ObjectUtils.isEmpty(userByRoleCodeAndCompanyId.getResult())){
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }
        List<SysUserDTO> result = userByRoleCodeAndCompanyId.getResult();
        return ResponseResult.success(result);
    }




}
