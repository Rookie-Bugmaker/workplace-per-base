package com.crrc.service;

import com.crrc.entity.HrEmpExportTask;
import com.crrc.pojo.vo.ExportTaskStatusVO;

/**
 * 人事信息异步导出任务 Service
 */
public interface HrEmpExportTaskService {

    /**
     * 创建导出任务
     *
     * @param taskType 任务类型
     * @param requestParamJson 请求参数JSON
     * @return 任务实体
     */
    HrEmpExportTask createTask(String taskType, String requestParamJson);

    /**
     * 更新任务状态为RUNNING
     *
     * @param taskId 任务ID
     */
    void updateRunning(String taskId);

    /**
     * 初始化总数量
     *
     * @param taskId 任务ID
     * @param totalCount 总数量
     */
    void initTotal(String taskId, int totalCount);

    /**
     * 增加已处理数量并更新进度
     *
     * @param taskId 任务ID
     * @param finishedDelta 增量
     */
    void incrementFinished(String taskId, int finishedDelta);

    /**
     * 设置已处理数量并更新进度
     *
     * @param taskId 任务ID
     * @param finishedCount 已处理数量
     */
    void updateFinished(String taskId, int finishedCount);

    /**
     * 更新进度
     *
     * @param taskId 任务ID
     * @param progress 进度值
     */
    void updateProgress(String taskId, int progress);

    /**
     * 任务成功
     *
     * @param taskId 任务ID
     * @param fileId 文件ID
     * @param fileName 文件名
     */
    void updateSuccess(String taskId, String fileId, String fileName);

    /**
     * 任务失败
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @param progress 失败时的进度
     */
    void updateFailed(String taskId, String errorMessage, int progress);

    /**
     * 根据taskId查询任务
     *
     * @param taskId 任务ID
     * @return 任务实体
     */
    HrEmpExportTask getByTaskId(String taskId);

    /**
     * 查询任务状态VO
     *
     * @param taskId 任务ID
     * @return 状态VO
     */
    ExportTaskStatusVO queryStatus(String taskId);
}
