package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpExportTask;
import com.crrc.enums.ExportTaskStatusEnum;
import com.crrc.mapper.HrEmpExportTaskMapper;
import com.crrc.pojo.vo.ExportTaskStatusVO;
import com.crrc.service.HrEmpExportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 人事信息异步导出任务 ServiceImpl
 */
@Service
public class HrEmpExportTaskServiceImpl implements HrEmpExportTaskService {

    public static final int PROGRESS_START = 1;

    public static final int QUERY_START_PROGRESS = 5;
    public static final int EXPORT_PRERESOLVE_END_PROGRESS = 12;
    public static final int TRANSLATE_END_PROGRESS = 15;
    public static final int QUERY_END_PROGRESS = 50;

    public static final int WRITE_START_PROGRESS = 50;
    public static final int WRITE_END_PROGRESS = 90;

    public static final int FINISH_EXCEL_PROGRESS = 95;
    public static final int UPLOAD_PROGRESS = 99;
    public static final int SUCCESS_PROGRESS = 100;

    @Autowired
    private HrEmpExportTaskMapper hrEmpExportTaskMapper;

    private final Random randomGenerator = new java.util.Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HrEmpExportTask createTask(String taskType, String requestParamJson) {
        HrEmpExportTask task = new HrEmpExportTask();
        task.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        task.setTaskId(generateTaskId());
        task.setTaskType(taskType);
        task.setStatus(ExportTaskStatusEnum.PENDING.getCode());
        task.setProgress(0);
        task.setTotalCount(null);
        task.setFinishedCount(0);
        task.setRequestParam(requestParamJson);
        task.setRetryCount(0);
        task.setMaxRetryCount(0);
        task.setCancelFlag("0");
        hrEmpExportTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRunning(String taskId) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(ExportTaskStatusEnum.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());
        hrEmpExportTaskMapper.updateById(task);
    }

    public int calculateStageProgress(int startProgress,
                                       int endProgress,
                                       int finished,
                                       int total) {
        if (total <= 0) {
            return endProgress;
        }

        int range = endProgress - startProgress;
        int progress = startProgress + (int) (finished * 1L * range / total);

        if (progress > endProgress) {
            progress = endProgress;
        }

        if (progress < startProgress) {
            progress = startProgress;
        }

        return progress;
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTotal(String taskId, int totalCount) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }

        task.setTotalCount(totalCount);
        task.setFinishedCount(0);

        int currentProgress = nullToZero(task.getProgress());

        if (totalCount <= 0) {
            // 没有可写入数据，认为写入阶段已经完成
            task.setProgress(Math.max(currentProgress, WRITE_END_PROGRESS));
        } else {
            // 写入阶段从 50% 开始，不允许倒退
            task.setProgress(Math.max(currentProgress, WRITE_START_PROGRESS));
        }

        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementFinished(String taskId, int finishedDelta) {
        if (finishedDelta <= 0) {
            return;
        }

        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }

        int oldFinished = task.getFinishedCount() == null ? 0 : task.getFinishedCount();
        int total = task.getTotalCount() == null ? 0 : task.getTotalCount();

        int newFinished = oldFinished + finishedDelta;

        if (total > 0 && newFinished > total) {
            newFinished = total;
        }

        task.setFinishedCount(newFinished);

        int currentProgress = nullToZero(task.getProgress());
        int calculatedProgress = calculateStageProgress(
                WRITE_START_PROGRESS,
                WRITE_END_PROGRESS,
                newFinished,
                total
        );

        task.setProgress(Math.max(currentProgress, calculatedProgress));

        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinished(String taskId, int finishedCount) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }

        int total = task.getTotalCount() == null ? 0 : task.getTotalCount();

        int safeFinished = Math.max(finishedCount, 0);
        if (total > 0 && safeFinished > total) {
            safeFinished = total;
        }

        task.setFinishedCount(safeFinished);

        int currentProgress = nullToZero(task.getProgress());
        int calculatedProgress = calculateStageProgress(
                WRITE_START_PROGRESS,
                WRITE_END_PROGRESS,
                safeFinished,
                total
        );

        task.setProgress(Math.max(currentProgress, calculatedProgress));

        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(String taskId, int progress) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }

        int currentProgress = nullToZero(task.getProgress());
        int safeProgress = Math.max(0, Math.min(progress, 100));

        // 进度只能递增，不能倒退
        if (safeProgress <= currentProgress) {
            return;
        }

        task.setProgress(safeProgress);
        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSuccess(String taskId, String fileId, String fileName) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(ExportTaskStatusEnum.SUCCESS.getCode());
        task.setProgress(100);
        task.setFileId(fileId);
        task.setFileName(fileName);
        task.setFinishedCount(task.getTotalCount());
        task.setFinishTime(LocalDateTime.now());
        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFailed(String taskId, String errorMessage, int progress) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }

        int currentProgress = nullToZero(task.getProgress());
        int safeProgress = Math.max(0, Math.min(progress, 100));

        task.setStatus(ExportTaskStatusEnum.FAILED.getCode());
        task.setProgress(Math.max(currentProgress, safeProgress));
        task.setErrorMessage(errorMessage);
        task.setFinishTime(LocalDateTime.now());

        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    public HrEmpExportTask getByTaskId(String taskId) {
        LambdaQueryWrapper<HrEmpExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpExportTask::getTaskId, taskId);
        return hrEmpExportTaskMapper.selectOne(wrapper);
    }

    @Override
    public ExportTaskStatusVO queryStatus(String taskId) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return null;
        }
        ExportTaskStatusVO vo = new ExportTaskStatusVO();
        vo.setTaskId(task.getTaskId());
        vo.setStatus(task.getStatus());
        vo.setProgress(task.getProgress());
        vo.setFinishedCount(task.getFinishedCount());
        vo.setTotalCount(task.getTotalCount());
        vo.setFileId(task.getFileId());
        vo.setFileName(task.getFileName());
        vo.setErrorMessage(task.getErrorMessage());
        return vo;
    }

    /**
     * 生成任务ID：EXP + yyyyMMddHHmmss + 6位随机数
     */
    private String generateTaskId() {
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (randomGenerator.nextInt() * 900000) + 100000;
        return "EXP" + timestamp + random;
    }
}
