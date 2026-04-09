package com.crrc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.dto.*;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.SysUserRequestDTO;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.utils.FlowableUtils;
import com.crrc.utils.TreeUtils;
import io.jsonwebtoken.lang.Objects;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private WkDataAPi wkDataAPi;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private WkCoreAPi wkCoreAPi;

    @Value("${crrc.role.audit.emp.sub.id}")
    private Long HRSC_SUB_EMP_INFO_MGR_ROLE_ID;

    @Value("${crrc.role.audit.emp.dept.id}")
    private Long HRSC_DEPT_EMP_INFO_MGR_ROLE_ID;
    @Autowired
    private FlowableUtils flowableUtils;

    @TenantIgnore
    @GetMapping("/findApprovalByRoleIdList")
    public ResponseResult<PaginationGrid<com.crrc.entity.dto.SysUserDTO>> findApprovalByRoleId(
            Integer page, Integer pageSize, @RequestParam("roleIdList") List<Long> roleIdList
    ) {
        if (ObjectUtils.isEmpty(roleIdList)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleIdList不能为空");
        }
        return ResponseResult.success(doFindApprovalByRoleIdList(roleIdList));
    }

    private PaginationGrid<com.crrc.entity.dto.SysUserDTO> doFindApprovalByRoleIdList(List<Long> roleIdList) {
        Set<com.crrc.entity.dto.SysUserDTO> userSet = new HashSet<>();
        roleIdList.forEach(roleId -> {
            List<com.crrc.entity.dto.SysUserDTO> roleUserListByRoleId = wkCoreAPi.getRoleUserListByRoleId(roleId);
            userSet.addAll(roleUserListByRoleId);
        });


        PaginationGrid<com.crrc.entity.dto.SysUserDTO> result = new PaginationGrid<>();
        result.setRows(new ArrayList<>(userSet));
        result.setTotal((long) userSet.size());

        return result;
    }

    @TenantIgnore
    @GetMapping("/findApprovalByRoleId")
    public ResponseResult<PaginationGrid<com.crrc.entity.dto.SysUserDTO>> findApprovalByRoleId(
            Integer page, Integer pageSize, Long roleId
    ) {
        if (ObjectUtils.isEmpty(roleId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleId不能为空");
        }
        return ResponseResult.success(doFindApprovalByRoleId(roleId));
    }

    private PaginationGrid<com.crrc.entity.dto.SysUserDTO> doFindApprovalByRoleId(Long roleId) {
        List<com.crrc.entity.dto.SysUserDTO> approval = wkCoreAPi.getRoleUserListByRoleId(roleId);

        PaginationGrid<com.crrc.entity.dto.SysUserDTO> result = new PaginationGrid<>();

        result.setRows(approval);
        result.setTotal((long) approval.size());

        return result;
    }

    @TenantIgnore
    @GetMapping("/findApprovalByRoleIdAndDeptCode")
    public ResponseResult<PaginationGrid<com.crrc.entity.dto.SysUserDTO>> findApprovalByRoleIdAndDeptCode(
            Long roleId, String deptCode
    ) {
        if (ObjectUtils.isEmpty(roleId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleId不能为空");
        }
        if (ObjectUtils.isEmpty(deptCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "deptCode不能为空");
        }
        return ResponseResult.success(doFindApprovalByRoleIdAndDeptCode(roleId, deptCode));
    }

    public PaginationGrid<com.crrc.entity.dto.SysUserDTO> doFindApprovalByRoleIdAndDeptCode(Long roleId, String deptCode) {
        List<com.crrc.entity.dto.SysUserDTO> roleUserListByRoleId = wkCoreAPi.getRoleUserListByRoleId(roleId);

        List<String> userCodes = roleUserListByRoleId.stream().map(com.crrc.entity.dto.SysUserDTO::getUserName).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(HrEmpBasic::getUserCode, userCodes);

        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.getBaseMapper().selectList(wrapper);

        Set<String> leaders = hrEmpBasicList.stream()
                .filter(emp -> Objects.nullSafeEquals(deptCode, emp.getDept()))
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toSet());

        List<com.crrc.entity.dto.SysUserDTO> sysUsers = roleUserListByRoleId.stream()
                .filter(user -> leaders.contains(user.getUserName()))
                .collect(Collectors.toList());

        PaginationGrid<com.crrc.entity.dto.SysUserDTO> result = new PaginationGrid<>();

        result.setRows(sysUsers);
        result.setTotal((long) sysUsers.size());

        return result;
    }

    @PostMapping("/batchApproval")
    @Operation(summary = "批量审批")
    public ResponseResult<String> batchApproval(@RequestBody List<String> processInstanceIds) {
        for (String processInstanceId : processInstanceIds) {
            flowableUtils.done(processInstanceId, "批量同意");
        }
        return ResponseResult.success("OK");
    }

    @PostMapping("/batchReject")
    @Operation(summary = "批量审批拒绝")
    public ResponseResult<String> batchReject(@RequestBody List<String> processInstanceIds) {
        for (String processInstanceId : processInstanceIds) {
            flowableUtils.refuse(processInstanceId, "批量不同意");
        }
        return ResponseResult.success("OK");
    }

    @GetMapping("/findUserByDeptIdAndRoleCode")
    @Operation(summary = "HrEmpFlowableController工作流获取审批人信息")
    @AuditLog(value = "HrEmpFlowableController工作流获取审批人信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<SysUserDTO>> findUserByDeptIdAndRoleCode(String companyCode, String deptCode, String roleCode) {
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode 查询参数companyCode:{},deptId:{},roleCode:{}", companyCode, deptCode, roleCode);
        if (ObjectUtils.isEmpty(deptCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "deptCode不能为空");
        }
        if (ObjectUtils.isEmpty(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }
        if (ObjectUtils.isEmpty(companyCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "companyCode不能为空");
        }
        List<SysUserDTO> result = new ArrayList<>();
        // 查找字典中配置的跨部门审批人的数据
        List<CodeInfo> codeInfoList = codeInfoService.getCodeInfoList("hr_emp_basic", "info_manager");

        // 获取所有的组织数据
        ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
        if (listResponseResultDTO == null || org.apache.commons.lang3.ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
            throw new IllegalArgumentException("外部接口错误，获取树形组织信息失败");
        }
        List<OrganizationTreeDTO> data = listResponseResultDTO.getData();

        // 获取包含deptCode的所有父节点
        Optional<List<OrganizationTreeDTO>> parents = Optional.empty();
        for(OrganizationTreeDTO dto : data) {
            parents = TreeUtils.findParents(dto, e -> deptCode.equals(e.getOrgCode()), OrganizationTreeDTO::getChildren);
            if(parents.isPresent()) {
                break;
            }
        }

        Set<String> parentOrgCodeSet = parents.get().stream().map(OrganizationTreeDTO::getOrgCode).collect(Collectors.toSet());

        // 由于业务要求，要求非本部门的人能够担任本部门的人事员，故有字典配置项
        // 这里itemName是deptCode，itemCode是userCode
        List<CodeInfo> auditList = codeInfoList.stream().filter(e -> parentOrgCodeSet.contains(e.getCode())).collect(Collectors.toList());
        if(!ObjectUtils.isEmpty(auditList)) {
            // 添加所有的字典审批人
            List<UserListDTO> list = wkCoreAPi.getUserListByUserName(auditList.stream().map(CodeInfo::getCodeValue).collect(Collectors.toList()));
            result.addAll(list.stream().map(e -> {
                SysUserDTO sysUserDTO = new SysUserDTO();
                sysUserDTO.setUserId(e.getId());
                sysUserDTO.setUserName(e.getUserName());
                sysUserDTO.setFullName(e.getNickName());
                return sysUserDTO;
            }).collect(Collectors.toList()));
        }

        Set<String> auditUserCodeList = auditList.stream().map(CodeInfo::getCodeValue).collect(Collectors.toSet());

        // 如果是子公司人员信息管理
        if("hrsc_sub_emp_info_mgr".equals(roleCode)) {
            ResultData<List<SysUserDTO>> resultData = sysServerClient.findUserByRoleId(HRSC_SUB_EMP_INFO_MGR_ROLE_ID);
            if(resultData == null || ObjectUtils.isEmpty(resultData.getResult())) {
                log.info("子公司人员信息管理角色查询为空:{}", resultData);
            }

            List<SysUserDTO> userByRoleId = resultData.getResult();

            // 排除在字典中配置过的人员
            List<String> userCodeList = userByRoleId.stream().map(SysUserDTO::getUserName).filter(userName -> !auditUserCodeList.contains(userName)).collect(Collectors.toList());

            List<HrEmpBasicDTO> hrEmpBasicDTOList = hrEmpBasicService.selectMsgList(userCodeList);

            // 筛选部门在其中的所有人员
            Set<String> userCodeSet = hrEmpBasicDTOList.stream().filter(
                    e -> parentOrgCodeSet.contains(e.getDept())
            ).map(HrEmpBasicDTO::getUserCode).collect(Collectors.toSet());

            result.addAll(userByRoleId.stream().filter(e -> userCodeSet.contains(e.getUserName())).collect(Collectors.toList()));
        }

        // 如果是部门人员信息管理
        if("hrsc_dept_emp_info_mgr".equals(roleCode)) {
            ResultData<List<SysUserDTO>> resultData = sysServerClient.findUserByRoleId(HRSC_DEPT_EMP_INFO_MGR_ROLE_ID);
            if(resultData == null || ObjectUtils.isEmpty(resultData.getResult())) {
                log.info("部门人员信息管理角色查询为空:{}", resultData);
            }

            List<SysUserDTO> userByRoleId = resultData.getResult();
            // 排除在字典中配置过的人员
            List<String> userCodeList = userByRoleId.stream().map(SysUserDTO::getUserName).filter(userName -> !auditUserCodeList.contains(userName)).collect(Collectors.toList());

            List<HrEmpBasicDTO> hrEmpBasicDTOList = hrEmpBasicService.selectMsgList(userCodeList);

            Set<String> userCodeSet = hrEmpBasicDTOList.stream().filter(
                e -> parentOrgCodeSet.contains(e.getDept())
            ).map(HrEmpBasicDTO::getUserCode).collect(Collectors.toSet());

            result.addAll(userByRoleId.stream().filter(e -> userCodeSet.contains(e.getUserName())).collect(Collectors.toList()));
        }

        if(ObjectUtils.isEmpty(result)) {
            log.info("未查到审批人数据");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }

        return ResponseResult.success(result);
    }

    @GetMapping("/responsibleUnit")
    @Operation(summary = "HrEmpFlowableController工作流获取审批人信息")
    @AuditLog(value = "HrEmpFlowableController工作流获取审批人信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<SysUserDTO>> responsibleUnit(
            String companyCode,
            String roleCode,
            String responsibleUnit
    ) {

        log.info("查询参数 companyCode:{}, roleCode:{}, responsibleUnit:{}",
                companyCode, roleCode, responsibleUnit);



        List<SysUserDTO> result;

        // ================== ⭐ 归口单位 → roleCode ==================
        Map<String, String> roleMap = new HashMap<>();
        roleMap.put("1", "484734531758022656");   // 党委组织部
        roleMap.put("2", "484734725752971264");   // 宣传部
        roleMap.put("3", "484734899485237248");       // 团委
        roleMap.put("4", "484735049175752704");       // 工会
        roleMap.put("5", "484735169137041408");          // 人力资源部
        roleMap.put("6", "484735447135510528");          // 其他

        if (StringUtils.isNotBlank(responsibleUnit)) {
            roleCode = roleMap.get(responsibleUnit);
        }

        if (StringUtils.isBlank(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }

        // ================== ⭐ 调用新接口 ==================

        ResultData<List<SysUserDTO>> resultData = sysServerClient.findUserByRoleId(Long.valueOf(roleCode));

        if (resultData == null || ObjectUtils.isEmpty(resultData.getResult())) {
            log.info("角色查询为空:{}", roleCode);
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }

        result = resultData.getResult();

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
