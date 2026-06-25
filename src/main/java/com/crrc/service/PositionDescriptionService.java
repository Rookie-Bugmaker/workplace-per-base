package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PositionDescription;
import com.crrc.pojo.dto.PositionDescriptionAuditDTO;
import com.crrc.pojo.dto.PositionDescriptionSaveDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionDescriptionExportBundleVO;
import com.crrc.pojo.vo.PositionDescriptionMainExportVO;
import com.crrc.pojo.vo.PositionDescriptionTodoVO;
import com.crrc.pojo.vo.PositionDescriptionWorkflowRoleVO;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;

import java.util.List;
import java.util.Map;

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
     * 启用已停用的说明书。
     */
    boolean enable(Long id);

    /**
     * 删除已撤销/取消的说明书。
     */
    boolean deleteCanceled(Long id);

    /**
     * 获取运行时流程图。
     */
    ProcessRunningInfoVo runningSequenceGraph(Long id);

    /**
     * 按查询条件导出职位说明书主信息列表。
     */
    List<PositionDescriptionMainExportVO> exportMainList(PositionDescription queryReqVO);

    /**
     * 构建职位说明书多 Sheet 导出数据。
     */
    PositionDescriptionExportBundleVO buildExportBundle(Long id);

    /**
     * 查询职位说明书流程角色配置。
     */
    List<PositionDescriptionWorkflowRoleVO> workflowRoles();

    /**
     * 当前登录用户职位说明书审批待办。
     */
    List<PositionDescriptionTodoVO> currentUserTodo();

    /**
     * 我的职位说明书申请分页。
     */
    void myCreatedPage(Page<PositionDescription> page, PositionDescription queryReqVO);

    /**
     * 按状态统计职位说明书数量。
     */
    Map<String, Long> statusStatistics(PositionDescription queryReqVO);

    /**
     * 取消/撤销职位说明书。审批中会同步终止 Flowable 流程。
     */
    boolean cancel(Long id, String reason);

}
