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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTotal(String taskId, int totalCount) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        task.setTotalCount(totalCount);
        task.setFinishedCount(0);
        task.setProgress(5);
        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementFinished(String taskId, int finishedDelta) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        int newFinished = (task.getFinishedCount() == null ? 0 : task.getFinishedCount()) + finishedDelta;
        task.setFinishedCount(newFinished);
        int total = task.getTotalCount() == null ? 1 : task.getTotalCount();
        if (total > 0) {
            // 5%~90% 的进度区间，按真实数据量计算
            int progress = 5 + (int) (newFinished * 85L / total);
            if (progress > 90) {
                progress = 90;
            }
            task.setProgress(progress);
        }
        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinished(String taskId, int finishedCount) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        task.setFinishedCount(finishedCount);
        int total = task.getTotalCount() == null ? 1 : task.getTotalCount();
        if (total > 0) {
            int progress = 5 + (int) (finishedCount * 85L / total);
            if (progress > 90) {
                progress = 90;
            }
            task.setProgress(progress);
        }
        hrEmpExportTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(String taskId, int progress) {
        HrEmpExportTask task = getByTaskId(taskId);
        if (task == null) {
            return;
        }
        // 进度只能递增，不能倒退
        if (progress <= task.getProgress()) {
            return;
        }
        task.setProgress(progress);
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
        task.setStatus(ExportTaskStatusEnum.FAILED.getCode());
        task.setProgress(progress);
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
