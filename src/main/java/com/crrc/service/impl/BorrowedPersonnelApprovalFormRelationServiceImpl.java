package com.crrc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.TenantIgnore;
import com.crrc.api.WkWorkflowApi;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.*;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.mapper.BorrowedPersonnelApprovalFormRelationMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.BorrowedPersonnelApprovalFormRelationDetailDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.RequestAndBorrowedConcatVO;
import com.crrc.service.BorrowedPersonnelApprovalFormRelationService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.TreeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    WkWorkflowApi wkWorkflowApi;
    @Autowired
    private PersonnelSecondmentRequestRelationServiceImpl personnelSecondmentRequestRelationService;
    @Autowired
    private TemplateGenerateServiceImpl templateGenerateService;
    @Autowired
    private BorrowedPersonnelApprovalFormServiceImpl borrowedPersonnelApprovalFormService;
    @Autowired
    private ExceptionCasePersonServiceImpl exceptionCasePersonService;

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
        formData.put("borrowingUnit", formRelation.getBorrowingUnit());
        formData.put("lendingUnit", formRelation.getLendingUnit());

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
    public void pageRequestBySourceDept(Page<RequestAndBorrowedConcatVO> page, PersonnelSecondmentRequestRelation pageReqVO) {
        personnelSecondmentRequestRelationService.pageBySourceDept(page, pageReqVO);
    }

    @Override
    public void saveDetails(String requestId, BorrowedPersonnelApprovalFormRelationDetailDTO reqDTO) {
        BorrowedPersonnelApprovalFormRelation relation = reqDTO.getRelation();
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

            // 推送的exceptionCasePerson
            List<BorrowedPersonnelApprovalForm> formList = borrowedPersonnelApprovalFormService.getByRelationId(relation.getId());
            Map<String, OrganizationTreeDTO> dept2ParentOrgCodeCacheMap = new HashMap<>();
            List<OrganizationTreeDTO> organizationTreeDTOList = DeptUtils.treeOrganization();

            List<ExceptionCasePerson> exceptionCasePeopleList = formList.stream().map(form -> {
                ExceptionCasePerson exceptionCasePerson = new ExceptionCasePerson();
                exceptionCasePerson.setId(IdUtil.randomUUID());
                exceptionCasePerson.setRelationId(form.getId());
                exceptionCasePerson.setName(form.getName());
                exceptionCasePerson.setUserCode(form.getUserCode());
                exceptionCasePerson.setFormerDept(relation.getLendingUnitName());
                exceptionCasePerson.setFormerDeptCode(relation.getLendingUnit());
                // 回写公司
                setOldCompany(dept2ParentOrgCodeCacheMap, relation.getLendingUnit(), exceptionCasePerson, organizationTreeDTOList);

                exceptionCasePerson.setNewDept(relation.getBorrowingUnitName());
                exceptionCasePerson.setNewDeptCode(relation.getBorrowingUnit());

                // 回写公司
                setNewCompany(dept2ParentOrgCodeCacheMap, relation.getBorrowingUnit(), exceptionCasePerson, organizationTreeDTOList);

                exceptionCasePerson.setPosition(form.getJobType());

                exceptionCasePerson.setStartDate(relation.getStartDate());
                exceptionCasePerson.setEndDate(relation.getEndDate());
                exceptionCasePerson.setSourceType("INT_LOAN");

                return exceptionCasePerson;
            }).collect(Collectors.toList());

            exceptionCasePersonService.saveBatch(exceptionCasePeopleList);
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

    private void setNewCompany(Map<String, OrganizationTreeDTO> dept2ParentOrgCodeCacheMap, String unit, ExceptionCasePerson exceptionCasePerson, List<OrganizationTreeDTO> organizationTreeDTOList) {
        if(dept2ParentOrgCodeCacheMap.containsKey(unit)) {
            exceptionCasePerson.setNewCompany(dept2ParentOrgCodeCacheMap.get(unit).getOrgName());
            exceptionCasePerson.setNewCompanyCode(dept2ParentOrgCodeCacheMap.get(unit).getOrgCode());
        } else {
            OrganizationTreeDTO parent = new OrganizationTreeDTO();
            for(OrganizationTreeDTO dto : organizationTreeDTOList) {
                if(
                        TreeUtils.containsBfs(
                                dto,
                                match -> unit.equals(match.getOrgCode()),
                                OrganizationTreeDTO::getChildren
                        ).equals(Boolean.TRUE)
                ) {
                    parent = dto;
                    break;
                }
            }

            dept2ParentOrgCodeCacheMap.put(unit, parent);
            exceptionCasePerson.setNewCompany(parent.getOrgName());
            exceptionCasePerson.setNewCompanyCode(parent.getOrgCode());
        }
    }

    @Override
    @TenantIgnore
    public BorrowedPersonnelApprovalFormRelationDetailDTO getDetailsById(String id) {
        BorrowedPersonnelApprovalFormRelation relation = getById(id);
        List<BorrowedPersonnelApprovalForm> formList = borrowedPersonnelApprovalFormService.getByRelationId(id);
        BorrowedPersonnelApprovalFormRelationDetailDTO detailDTO = new BorrowedPersonnelApprovalFormRelationDetailDTO();
        detailDTO.setRelation(relation);
        detailDTO.setApprovalFormList(formList);
        return detailDTO;
    }

    private static void setOldCompany(Map<String, OrganizationTreeDTO> dept2ParentOrgCodeCacheMap, String unit, ExceptionCasePerson exceptionCasePerson, List<OrganizationTreeDTO> organizationTreeDTOList) {
        if(dept2ParentOrgCodeCacheMap.containsKey(unit)) {
            exceptionCasePerson.setFormerCompany(dept2ParentOrgCodeCacheMap.get(unit).getOrgName());
            exceptionCasePerson.setFormerCompanyCode(dept2ParentOrgCodeCacheMap.get(unit).getOrgCode());
        } else {
            OrganizationTreeDTO parent = new OrganizationTreeDTO();
            for(OrganizationTreeDTO dto : organizationTreeDTOList) {
                if(
                        TreeUtils.containsBfs(
                                dto,
                                match -> unit.equals(match.getOrgCode()),
                                OrganizationTreeDTO::getChildren
                        ).equals(Boolean.TRUE)
                ) {
                    parent = dto;
                    break;
                }
            }

            dept2ParentOrgCodeCacheMap.put(unit, parent);
            exceptionCasePerson.setFormerCompany(parent.getOrgName());
            exceptionCasePerson.setFormerCompanyCode(parent.getOrgCode());
        }
    }
}