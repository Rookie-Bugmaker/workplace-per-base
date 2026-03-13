package com.crrc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkWorkflowApi;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.*;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.mapper.BorrowedPersonnelApprovalFormRelationMapper;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.RequestAndBorrowedConcatVO;
import com.crrc.service.PersonnelSecondmentRequestRelationService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.SecurityFrameworkUtils;
import com.crrc.utils.WrapperUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    TemplateGenerateServiceImpl templateGenerateService;
    @Autowired
    BorrowedPersonnelApprovalFormRelationMapper borrowedPersonnelMapper;

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
        formData.put("requestingDepartment", relation.getRequestingDepartment());

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
        templateGenerateService.generateRequestWordAndUpdate(id, 0, 4);
        log.info("流程测试success");
        return new ResponseResult<>(HttpStatus.OK.value(), "审批流发起成功", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendToSourceDept(String relationId, List<String> deptCodeList) {
        // 1. 查询当前关系下已存在的部门关联
        LambdaQueryWrapper<BorrowedPersonnelApprovalFormRelation> existedRelationsWrapper = new LambdaQueryWrapper<>();
        existedRelationsWrapper.eq(BorrowedPersonnelApprovalFormRelation::getRequestId, relationId);
        List<BorrowedPersonnelApprovalFormRelation> existingRelations =
                borrowedPersonnelMapper.selectList(existedRelationsWrapper);

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

        for (BorrowedPersonnelApprovalFormRelation existingRelation : existingRelations) {
            String existingDeptCode = existingRelation.getTargetDept();

            // 以下两种情况需要删除：
            // 1) 该部门已不在本次传入列表中
            // 2) 数据库中存在重复 deptCode，只保留第一条，其余重复记录删除
            if (!targetDeptCodeSet.contains(existingDeptCode) || !retainedDeptCodeSet.add(existingDeptCode)) {
                removeIds.add(existingRelation.getId());
            }
        }

        // 4. 计算需要新增的数据：
        //    前端传入中有，但数据库最终保留集中没有的 deptCode，需要新增
        List<BorrowedPersonnelApprovalFormRelation> insertRelations = new ArrayList<>();
        for (String deptCode : targetDeptCodeSet) {
            if (!retainedDeptCodeSet.contains(deptCode)) {
                BorrowedPersonnelApprovalFormRelation newRelation = new BorrowedPersonnelApprovalFormRelation();
                newRelation.setId(IdUtil.randomUUID());
                newRelation.setRequestId(relationId);
                newRelation.setTargetDept(deptCode);
                insertRelations.add(newRelation);
            }
        }

        // 5. 批量删除与批量新增
        //    非空时再执行，避免无意义数据库调用
        if (!removeIds.isEmpty()) {
            borrowedPersonnelMapper.deleteBatchIds(removeIds);
        }

        if (!insertRelations.isEmpty()) {
            for (BorrowedPersonnelApprovalFormRelation borrowedPersonnelApprovalFormRelation : insertRelations) {
                borrowedPersonnelMapper.insert(borrowedPersonnelApprovalFormRelation);
            }
        }

        return true;
    }

    @Override
    public void pageBySourceDept(Page<RequestAndBorrowedConcatVO> page, PersonnelSecondmentRequestRelation pageReqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if(loginUser == null) {
            throw new IllegalStateException("登录用户信息获取失败");
        }
        User user = loginUser.getUser();
        String jobNo = user.getJobNo();

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpBasic::getUserCode, jobNo);
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicMapper.selectList(wrapper);

        String dept;
        if(hrEmpBasicList != null && !hrEmpBasicList.isEmpty()) {
            HrEmpBasic hrEmpBasic = hrEmpBasicList.get(0);
            dept = hrEmpBasic.getDept();
        } else {
            dept = "0";
        }

        MPJLambdaWrapper<PersonnelSecondmentRequestRelation> mpjLambdaWrapper = new MPJLambdaWrapper<PersonnelSecondmentRequestRelation>()
                .selectAll(PersonnelSecondmentRequestRelation.class);

        WrapperUtils.selectAllWithPrefix(mpjLambdaWrapper, BorrowedPersonnelApprovalFormRelation.class, "t1", "brw_");

        mpjLambdaWrapper
                .innerJoin(
                        BorrowedPersonnelApprovalFormRelation.class, on -> on
                                .eq(BorrowedPersonnelApprovalFormRelation::getRequestId, PersonnelSecondmentRequestRelation::getId)
                )
                .eq(BorrowedPersonnelApprovalFormRelation::getTargetDept, dept);

        baseMapper.selectJoinPage(page, RequestAndBorrowedConcatVO.class, mpjLambdaWrapper);
    }
}