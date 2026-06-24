package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PositionDescription;
import com.crrc.pojo.dto.PositionDescriptionAuditDTO;
import com.crrc.pojo.dto.PositionDescriptionSaveDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionDescriptionWorkflowRoleVO;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;

import java.util.List;

/**
 * 职位说明书 服务类。
 */
public interface PositionDescriptionService extends IService<PositionDescription> {

    /**
     * 分页查询职位说明书主表。
     */
    void queryPage(Page<PositionDescription> page, PositionDescription queryReqVO);

    /**
     * 查询职位说明书主表列表。
     */
    List<PositionDescription> queryList(PositionDescription queryReqVO);

    /**
     * 保存草稿或驳回后修改。
     */
    PositionDescriptionDetailVO saveDraft(PositionDescriptionSaveDTO request);

    /**
     * 聚合查询职位说明书详情。
     */
    PositionDescriptionDetailVO getDetail(Long id);

    /**
     * 查询职位当前最新说明书详情。
     */
    PositionDescriptionDetailVO getLatestByPositionCode(String positionCode);

    /**
     * 提交 Flowable 审批。
     */
    PositionDescription submit(Long id);

    /**
     * Flowable 审批通过。
     */
    boolean approve(PositionDescriptionAuditDTO request);

    /**
     * Flowable 审批驳回。
     */
    boolean reject(PositionDescriptionAuditDTO request);

    /**
     * Flowable 回调，发布或驳回说明书。
     */
    boolean workflowCallback(WorkflowWriteBackParamDTO param);

    /**
     * 发布说明书。
     */
    boolean publish(Long id);

    /**
     * 停用说明书。
     */
    boolean disable(Long id);

    /**
     * 获取运行时流程图。
     */
    ProcessRunningInfoVo runningSequenceGraph(Long id);

    /**
     * 查询职位说明书流程角色配置。
     */
    List<PositionDescriptionWorkflowRoleVO> workflowRoles();
}
