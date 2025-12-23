package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.api.WkCoreAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.PositionSetupApplication;
import com.crrc.entity.dto.RoleDTO;
import com.crrc.entity.dto.UserDTO;
import com.crrc.mapper.PositionSetupApplicationMapper;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.SysUserRequestDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.PositionSetupApplicationService;
import com.crrc.utils.ConstantsUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import com.crrc.pojo.dto.WorkflowUserPageDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author yukun
 * @since 2025-12-19 11:03:43
 */
@Service
public class PositionSetupApplicationServiceImpl extends WorkServiceImpl<PositionSetupApplicationMapper, PositionSetupApplication> implements PositionSetupApplicationService {

    private static final Long WMRZ_AUDIT_ROLE_ID = 416962338874359808L;

    private final SysServerClient sysServerClient;

    public PositionSetupApplicationServiceImpl(SysServerClient sysServerClient) {
        this.sysServerClient = sysServerClient;
    }

    @Override
    public WorkflowUserPageDTO findApproval(Integer page, Integer pageSize) {
        WorkflowUserPageDTO pageDTO = new WorkflowUserPageDTO();
        // TODO: 替换为真正的审批人查询逻辑
        ResultData<List<SysUserDTO>> resultData = sysServerClient.findUserByRoleCode(WMRZ_AUDIT_ROLE_ID);
        if(resultData == null || ObjectUtils.isEmpty(resultData.getResult())) {
            throw new IllegalArgumentException("审批人查询为空!");
        }
        List<SysUserDTO> rows = resultData.getResult();
        pageDTO.setRows(rows);
        pageDTO.setTotal((long) rows.size());

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

    @Override
    public void resolveFields(List<PositionSetupApplication> records) {
        Map<String, String> translateMap = ConstantsUtils.valueToDescMap(AuditConstants.class);
        records.forEach(item -> {
            item.setApprovalStatusValue(translateMap.getOrDefault(item.getApprovalStatus(), item.getApprovalStatus()));
        });
    }
}