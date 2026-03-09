package com.crrc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.common.PaginationGrid;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.BorrowedPersonnelApprovalForm;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.mapper.BorrowedPersonnelApprovalFormRelationMapper;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.BorrowedPersonnelApprovalFormRelationDetailDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.BorrowedPersonnelApprovalFormRelationService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.TreeUtils;
import io.jsonwebtoken.lang.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员审批表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
@Slf4j
public class BorrowedPersonnelApprovalFormRelationServiceImpl extends WorkServiceImpl<BorrowedPersonnelApprovalFormRelationMapper, BorrowedPersonnelApprovalFormRelation> implements BorrowedPersonnelApprovalFormRelationService {

    public static final String BORROWING_TEMPLATE_ALIAS = "BORROWING_AUDIT";
    @Value("${crrc.role.leader.borrowing.id}")
    private long borrowingLeaderRoleId;
    @Value("${crrc.role.hr.admin.quota.id}")
    private long quotaApprovalRoleId;

    @Autowired
    WkWorkflowApi wkWorkflowApi;
    @Autowired
    private PersonnelSecondmentRequestRelationServiceImpl personnelSecondmentRequestRelationService;
    @Autowired
    private TemplateGenerateServiceImpl templateGenerateService;
    @Autowired
    private BorrowedPersonnelApprovalFormServiceImpl borrowedPersonnelApprovalFormService;
    @Autowired
    WkCoreAPi wkCoreAPi;
    @Autowired
    HrEmpBasicMapper basicMapper;

    @Override
    public ResponseResult<Boolean> submit(String id) {
        // 查出header
        BorrowedPersonnelApprovalFormRelation formRelation = getById(id);

        // 4. 启动审批流程
        // 封装数据，提交审批流
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        instanceDTO.setBizCode(formRelation.getId());
        // 封装数据主键
        instanceDTO.setBizId(formRelation.getId());
        // 封装业务模板编码
        instanceDTO.setTemplateAlias(BORROWING_TEMPLATE_ALIAS);
        // 封装业务表单数据
        Map<String, Object> formData = new HashMap<>();

        formData.put("id", id);

        instanceDTO.setFormData(formData);
        log.info("流程测试instanceDTO: {}", instanceDTO);
        // 返回流程实例id
        ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
        log.info("流程测试processInstanceVo: {}", processInstanceVo);

        if(processInstanceVo == null || processInstanceVo.getResult() == null || processInstanceVo.getResult().getProcessInstanceId() == null) {
            throw new IllegalArgumentException(String.format("部门:%s 目前不存在主管领导", formRelation.getBorrowingUnit()));
        }

        // 5. 更新流程实例ID
        formRelation.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
        // 更新审批状态
        formRelation.setOrderStatus(AuditConstants.STATUS_AUDIT);

        saveOrUpdate(formRelation);

        // 生成表单
        templateGenerateService.generateApprovalFormWordAndUpdate(id, 0, 6);
        log.info("流程测试success");
        return new ResponseResult<>(HttpStatus.OK.value(), "审批流发起成功", true);

    }

    @Override
    public void pageRequestBySourceDept(Page<PersonnelSecondmentRequestRelation> page, PersonnelSecondmentRequestRelation pageReqVO) {
        personnelSecondmentRequestRelationService.pageBySourceDept(page, pageReqVO);
    }

    @Override
    public void saveDetails(String requestId, BorrowedPersonnelApprovalFormRelationDetailDTO reqDTO) {
        BorrowedPersonnelApprovalFormRelation relation = reqDTO.getRelation();

        PersonnelSecondmentRequestRelation request = personnelSecondmentRequestRelationService.getById(requestId);

        if(!StringUtils.hasText(relation.getId())) {
            String id = IdUtil.randomUUID();
            relation.setId(id);
            request.setPersonnelRelationId(id);
            request.setOrderStatus(AuditConstants.STATUS_AUDIT);
        }

        personnelSecondmentRequestRelationService.updateById(request);
        saveOrUpdate(relation);

        List<BorrowedPersonnelApprovalForm> reqVOList = reqDTO.getApprovalFormList();

        reqVOList.forEach(
                item -> {
                    if(!StringUtils.hasText(item.getId())) {
                        item.setId(IdUtil.randomUUID());
                        item.setRelationId(relation.getId());
                    }
                }
        );

        borrowedPersonnelApprovalFormService.saveDetails(relation.getId(), reqVOList);
    }

    @Override
    public PaginationGrid<SysUserDTO> findBorrowingUnitSupervisorApproval(String id) {
        // 查询出当前需要审批的计划
        BorrowedPersonnelApprovalFormRelation relation = getById(id);
        // 获得需求单位的编码
        String borrowingUnit = relation.getBorrowingUnit();

        List<SysUserDTO> roleUserListByRoleId = wkCoreAPi.getRoleUserListByRoleId(borrowingLeaderRoleId);

        List<String> userCodes = roleUserListByRoleId.stream().map(SysUserDTO::getUserName).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(HrEmpBasic::getUserCode, userCodes);

        List<HrEmpBasic> hrEmpBasicList = basicMapper.selectList(wrapper);

        Set<String> leaders = hrEmpBasicList.stream()
                .filter(emp -> Objects.nullSafeEquals(borrowingUnit, emp.getDept()))
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toSet());

        List<SysUserDTO> sysUsers = roleUserListByRoleId.stream()
                .filter(user -> leaders.contains(user.getUserName()))
                .collect(Collectors.toList());

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(sysUsers);
        result.setTotal((long) sysUsers.size());

        return result;
    }

    @Override
    public ResponseResult<Boolean> writeBack(WorkflowWriteBackParamDTO param) {
        BorrowedPersonnelApprovalFormRelation relation = getById(param.getBizId());
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
            LambdaUpdateWrapper<BorrowedPersonnelApprovalFormRelation> wrapper = new LambdaUpdateWrapper<>();
            // 重置状态
            wrapper.set(BorrowedPersonnelApprovalFormRelation::getOrderStatus, AuditConstants.STATUS_DRAFT);
            wrapper.set(BorrowedPersonnelApprovalFormRelation::getProcessInstanceId, null);
            wrapper.eq(BorrowedPersonnelApprovalFormRelation::getId, relation.getId());
            baseMapper.update(wrapper);
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", true);
    }

    @Override
    public PaginationGrid<SysUserDTO> findQuotaApproval() {

        List<SysUserDTO> quotaApproval = wkCoreAPi.getRoleUserListByRoleId(quotaApprovalRoleId);

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(quotaApproval);
        result.setTotal((long) quotaApproval.size());

        return result;
    }

    @Override
    public PaginationGrid<SysUserDTO> findCompanyLeaderApproval(String id) {
        // id暂时不用,默认都在长客
        List<SysUserDTO> quotaApproval = wkCoreAPi.getRoleUserListByRoleId(borrowingLeaderRoleId);

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(quotaApproval);
        result.setTotal((long) quotaApproval.size());

        return result;
    }
}