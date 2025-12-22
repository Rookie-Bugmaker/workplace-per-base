package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.PositionSetupApplication;
import com.crrc.mapper.PositionSetupApplicationMapper;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.SysUserRequestDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.PositionSetupApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import com.crrc.pojo.dto.WorkflowUserPageDTO;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yukun
 * @since 2025-12-19 11:03:43
 */
@Service
public class PositionSetupApplicationServiceImpl extends WorkServiceImpl<PositionSetupApplicationMapper, PositionSetupApplication> implements PositionSetupApplicationService {

    private final SysServerClient sysServerClient;

    public PositionSetupApplicationServiceImpl(SysServerClient sysServerClient) {
        this.sysServerClient = sysServerClient;
    }

    @Override
    public WorkflowUserPageDTO findApproval(Integer page, Integer pageSize) {
        WorkflowUserPageDTO pageDTO = new WorkflowUserPageDTO();
        // TODO: 替换为真正的审批人查询逻辑
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCode("wmrz_audit");
        ResultData<List<SysUserDTO>> userByRoleCode = sysServerClient.findUserByRoleCode(sysUserRequestDTO);
        if(userByRoleCode == null || ObjectUtils.isEmpty(userByRoleCode.getResult())) {
            throw new IllegalArgumentException("审批人查询为空!");
        }

        pageDTO.setRows(userByRoleCode.getResult());
        pageDTO.setTotal((long) userByRoleCode.getResult().size());

        return pageDTO;
    }

    @Override
    public void processWriteBack(WorkflowWriteBackParamDTO param) {
        PositionSetupApplication po = new PositionSetupApplication();
        po.setId(Long.valueOf(param.getBizId()));
        po.setWfId(param.getWfId());
        po.setProcessInstanceId(param.getProcessInstanceId());
        Integer processInstanceStatus = param.getProcessInstanceStatus();

        if (processInstanceStatus == 1) {
            // 进行中
            po.setApprovalStatus(AuditConstants.STATUS_AUDIT);
        } else if (processInstanceStatus == 2) {
            // 已结束
            po.setApprovalStatus(AuditConstants.STATUS_AUDIT_PASS);
        } else if (processInstanceStatus == 3) {
            // 已拒绝
            po.setApprovalStatus(AuditConstants.STATUS_REJECT);
        } else if (processInstanceStatus == 4) {
            // 已重置
            po.setApprovalStatus(AuditConstants.STATUS_DRAFT);
            po.setProcessInstanceId(null);
        }

        boolean result = updateById(po);
        if (!result) {
            throw new IllegalArgumentException("工作流回写失败, 数据保存异常");
        }
    }

    @Override
    public Boolean saveOrUpdateDraft(PositionSetupApplication updateReqVO) {
        updateReqVO.setApprovalStatus(AuditConstants.STATUS_DRAFT);
        return saveOrUpdate(updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeDraftByIds(List<Long> ids) {
        Set<String> filteredSet = new HashSet<>(Arrays.asList(
                AuditConstants.STATUS_AUDIT,
                AuditConstants.STATUS_AUDIT_PASS
        ));
        LambdaQueryWrapper<PositionSetupApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PositionSetupApplication::getId, ids);
        List<PositionSetupApplication> positionSetupApplications = this.baseMapper.selectList(queryWrapper);

        if
        (
            ObjectUtils.isEmpty(positionSetupApplications) ||
            positionSetupApplications.stream().anyMatch(e -> filteredSet.contains(e.getApprovalStatus()))
        ) {
            throw new IllegalArgumentException("该草稿已经被删除或已提交进入审批");
        }

        return removeBatchByIds(ids);
    }
}