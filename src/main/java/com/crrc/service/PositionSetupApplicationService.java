package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PositionSetupApplication;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.WorkflowUserPageDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;

import javax.validation.Valid;
import java.util.List;

/**
 *
 * @author yukun
 * @since 2025-12-19 11:03:43
 */
public interface PositionSetupApplicationService extends IService<PositionSetupApplication> {

    /**
     * 查询职位申请审批人
     * @param page 分页数据
     * @param pageSize 分页大小
     * @return 分页的审批人数据
     */
    WorkflowUserPageDTO findApproval(Integer page, Integer pageSize);

    /**
     * 处理工作流回写
     * @param param 工作流回写数据
     */
    void processWriteBack(WorkflowWriteBackParamDTO param);

    /**
     * 保存或更新草稿
     * @param updateReqVO 需要保存的草稿
     * @return 是否保存成功
     */
    Boolean saveOrUpdateDraft(@Valid PositionSetupApplication updateReqVO);

    /**
     * 根据id删除草稿，如果不是草稿会throw exception
     * @param id 需要删除的数据的id
     * @return 是否删除成功
     * @throws IllegalArgumentException 不是草稿状态会抛此异常
     */
    Boolean removeDraftById(Long id);
}