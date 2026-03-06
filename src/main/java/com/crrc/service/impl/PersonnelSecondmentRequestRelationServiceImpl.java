package com.crrc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.common.PaginationGrid;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.*;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.PersonnelSecondmentRequestMapper;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestRelationTemplateVO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestTemplateVO;
import com.crrc.service.PersonnelSecondmentRequestRelationService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.SecurityFrameworkUtils;
import com.crrc.utils.WordTemplateProcessor;
import com.crrc.utils.WrapperUtils;
import io.jsonwebtoken.lang.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
@Slf4j
public class PersonnelSecondmentRequestRelationServiceImpl extends WorkServiceImpl<PersonnelSecondmentRequestRelationMapper, PersonnelSecondmentRequestRelation> implements PersonnelSecondmentRequestRelationService {

    public static final String BORROW_TEMPLATE_ALIAS = "BORROW_AUDIT";
    @Autowired
    WkCoreAPi wkCoreAPi;
    @Autowired
    WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    TemplateGenerateServiceImpl templateGenerateService;
    @Autowired
    PersonnelSecondmentRequestSourceDeptRelServiceImpl personnelSecondmentRequestSourceDeptRelService;

    @Value("${crrc.role.leader.borrow.id}")
    private long borrowLeaderRoleId;
    @Value("${crrc.role.hr.admin.quota.id}")
    private long quotaApprovalRoleId;
    @Value("${crrc.role.hr.admin.dispatch.id}")
    private long dispatchApprovalRoleId;
    @Value("${crrc.role.hr.deputy.minister.id}")
    private long deputyMinisterRoleId;

    @Override
    public void resolveFields(List<PersonnelSecondmentRequestRelation> records) {
        Map<String, String> translateMap = records.stream().map(PersonnelSecondmentRequestRelation::getRequestingDepartment).distinct().collect(Collectors.toMap(
                k -> k,
                DeptUtils::getDeptNameByCode,
                (a, b) -> a
        ));

        records.forEach(item ->
            item.setRequestingDepartmentValue(
                translateMap.getOrDefault(item.getRequestingDepartment(), item.getRequestingDepartment())
            )
        );
    }

    @NotNull
    public ResponseResult<PaginationGrid<SysUserDTO>> findBorrowLeaderApproval(String id) {
        // 查询出当前需要审批的计划
        PersonnelSecondmentRequestRelation relation = getById(id);
        // 获得需求单位的编码
        String requestingDepartment = relation.getRequestingDepartment();

        List<SysUserDTO> roleUserListByRoleId = wkCoreAPi.getRoleUserListByRoleId(borrowLeaderRoleId);

        List<String> userCodes = roleUserListByRoleId.stream().map(SysUserDTO::getUserName).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(HrEmpBasic::getUserCode, userCodes);

        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicMapper.selectList(wrapper);

        Set<String> leaders = hrEmpBasicList.stream()
                .filter(emp -> Objects.nullSafeEquals(requestingDepartment, emp.getDept()))
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toSet());

        List<SysUserDTO> sysUsers = roleUserListByRoleId.stream()
                .filter(user -> leaders.contains(user.getUserName()))
                .collect(Collectors.toList());

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(sysUsers);
        result.setTotal((long) sysUsers.size());

        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult<Boolean> writeBack(WorkflowWriteBackParamDTO param) {
        PersonnelSecondmentRequestRelation relation = getById(param.getBizId());
        relation.setWfId(param.getWfId());
        relation.setProcessInstanceId(param.getProcessInstanceId());
        Integer processInstanceStatus = param.getProcessInstanceStatus();

        if (processInstanceStatus == 1) {
            // 进行中
            relation.setOrderStatus(AuditConstants.STATUS_AUDIT);
            updateById(relation);
        } else if (processInstanceStatus == 2) {
            // 已结束
            relation.setOrderStatus(AuditConstants.STATUS_AUDIT_PASS);
            updateById(relation);
        } else if (processInstanceStatus == 3) {
            // 已拒绝
            relation.setOrderStatus(AuditConstants.STATUS_REJECT);
            log.info("已拒绝");
            updateById(relation);
        } else if (processInstanceStatus == 4) {
            // 已重置
            log.info("已重置");
            LambdaUpdateWrapper<PersonnelSecondmentRequestRelation> wrapper = new LambdaUpdateWrapper<>();
            // 重置状态
            wrapper.set(PersonnelSecondmentRequestRelation::getOrderStatus, AuditConstants.STATUS_DRAFT);
            wrapper.set(PersonnelSecondmentRequestRelation::getProcessInstanceId, null);
            wrapper.eq(PersonnelSecondmentRequestRelation::getId, relation.getId());
            baseMapper.update(wrapper);
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> submit(String id) {
        // 查出header
        PersonnelSecondmentRequestRelation relation = getById(id);

        // 4. 启动审批流程
        // 封装数据，提交审批流
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        instanceDTO.setBizCode(relation.getId());
        // 封装数据主键
        instanceDTO.setBizId(relation.getId());
        // 封装业务模板编码
        instanceDTO.setTemplateAlias(BORROW_TEMPLATE_ALIAS);
        // 封装业务表单数据
        Map<String, Object> formData = new HashMap<>();

        formData.put("id", id);

        instanceDTO.setFormData(formData);
        log.info("流程测试instanceDTO: {}", instanceDTO);
        // 返回流程实例id
        ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
        log.info("流程测试processInstanceVo: {}", processInstanceVo);

        if(processInstanceVo == null || processInstanceVo.getResult() == null || processInstanceVo.getResult().getProcessInstanceId() == null) {
            throw new IllegalArgumentException(String.format("部门:%s 目前不存在审批人", relation.getRequestingDepartment()));
        }

        // 5. 更新流程实例ID
        relation.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
        // 更新审批状态
        relation.setOrderStatus(AuditConstants.STATUS_AUDIT);

        saveOrUpdate(relation);

        // 生成表单
        templateGenerateService.generateRequestWordAndUpdate(id);
        log.info("流程测试success");
        return new ResponseResult<>(HttpStatus.OK.value(), "审批流发起成功", true);
    }



    @Override
    public ResponseResult<PaginationGrid<SysUserDTO>> findHrAuditApproval() {
        // 定额管理人员与调配管理人员会签
        List<SysUserDTO> quotaApproval = wkCoreAPi.getRoleUserListByRoleId(quotaApprovalRoleId);
        List<SysUserDTO> dispatchApproval = wkCoreAPi.getRoleUserListByRoleId(dispatchApprovalRoleId);

        quotaApproval.addAll(dispatchApproval);

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(quotaApproval);
        result.setTotal((long) quotaApproval.size());

        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult<PaginationGrid<SysUserDTO>> findHrDeputyMinisterApproval() {
        List<SysUserDTO> rows = wkCoreAPi.getRoleUserListByRoleId(deputyMinisterRoleId);

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(rows);
        result.setTotal((long) rows.size());

        return ResponseResult.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendToSourceDept(String relationId, List<String> deptCodeList) {
        // 1. 查询当前关系下已存在的部门关联
        List<PersonnelSecondmentRequestSourceDeptRel> existingRelations =
                personnelSecondmentRequestSourceDeptRelService.getListByRelationId(relationId);

        // 2. 将前端传入的部门编码去重，避免重复插入
        //    LinkedHashSet 既能去重，也能保留原始顺序
        Set<String> targetDeptCodeSet = new LinkedHashSet<>();
        if (deptCodeList != null) {
            for (String deptCode : deptCodeList) {
                if (deptCode != null && !deptCode.isEmpty()) {
                    targetDeptCodeSet.add(deptCode);
                }
            }
        }

        // 3. 计算需要删除的旧数据，以及当前数据库中最终应保留的部门编码
        //    retainedDeptCodeSet：数据库中已存在且这次仍然需要保留的 deptCode
        //    removeIds：需要删除的关联记录 id
        Set<String> retainedDeptCodeSet = new HashSet<>();
        List<String> removeIds = new ArrayList<>();

        for (PersonnelSecondmentRequestSourceDeptRel existingRelation : existingRelations) {
            String existingDeptCode = existingRelation.getDeptCode();

            // 以下两种情况需要删除：
            // 1) 该部门已不在本次传入列表中
            // 2) 数据库中存在重复 deptCode，只保留第一条，其余重复记录删除
            if (!targetDeptCodeSet.contains(existingDeptCode) || !retainedDeptCodeSet.add(existingDeptCode)) {
                removeIds.add(existingRelation.getId());
            }
        }

        // 4. 计算需要新增的数据：
        //    前端传入中有，但数据库最终保留集中没有的 deptCode，需要新增
        List<PersonnelSecondmentRequestSourceDeptRel> insertRelations = new ArrayList<>();
        for (String deptCode : targetDeptCodeSet) {
            if (!retainedDeptCodeSet.contains(deptCode)) {
                PersonnelSecondmentRequestSourceDeptRel newRelation = new PersonnelSecondmentRequestSourceDeptRel();
                newRelation.setId(IdUtil.randomUUID());
                newRelation.setRelationId(relationId);
                newRelation.setDeptCode(deptCode);
                insertRelations.add(newRelation);
            }
        }

        // 5. 批量删除与批量新增
        //    非空时再执行，避免无意义数据库调用
        if (!removeIds.isEmpty()) {
            personnelSecondmentRequestSourceDeptRelService.removeBatchByIds(removeIds);
        }

        if (!insertRelations.isEmpty()) {
            personnelSecondmentRequestSourceDeptRelService.saveBatch(insertRelations);
        }

        return true;
    }

    @Override
    public void pageBySourceDept(Page<PersonnelSecondmentRequestRelation> page, PersonnelSecondmentRequestRelation pageReqVO) {
        User user = SecurityFrameworkUtils.getLoginUser().getUser();
        String jobNo = user.getJobNo();

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpBasic::getUserCode, jobNo);
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicMapper.selectList(wrapper);

        String dept = "0";
        if(hrEmpBasicList != null && !hrEmpBasicList.isEmpty()) {
            HrEmpBasic hrEmpBasic = hrEmpBasicList.get(0);
            dept = hrEmpBasic.getDept();
        }

        QueryWrapper<PersonnelSecondmentRequestRelation> relationWrapper = WrapperUtils.buildWrapper(pageReqVO, WrapperUtils.StringMode.LIKE);
        relationWrapper.exists(
                "select 1 " +
                        "from personnel_secondment_request_source_dept_rel r " +
                        "where r.relation_id = personnel_secondment_request_relation.id " +
                        "and r.dept_code = {0}",
                dept
        );

        baseMapper.selectPage(page, relationWrapper);
    }
}