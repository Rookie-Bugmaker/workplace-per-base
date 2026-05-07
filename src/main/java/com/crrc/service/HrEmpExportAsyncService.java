package com.crrc.service;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crrc.entity.ByteArrayMultipartFile;
import com.crrc.entity.HrEmpExportTask;
import com.crrc.enums.ExportTaskTypeEnum;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.pojo.dto.HrEmpBatchExportRequest;
import com.crrc.utils.MinIOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

/**
 * 人事信息异步导出执行服务
 *
 * 注意：本类使用独立的 Spring Bean，@Async 不会自调用失效。
 */
@Service
@Slf4j
public class HrEmpExportAsyncService {

    @Autowired
    private HrEmpExportTaskService hrEmpExportTaskService;

    @Autowired
    private HrEmpExportService hrEmpExportService;

    @Resource
    private MinIOUtils minIOUtils;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步执行导出任务
     *
     * @param taskId 任务ID
     */
    @Async("templateGenerateAsyncExecutor")
    public void executeAsync(String taskId) {
        log.info("开始执行导出任务, taskId={}", taskId);
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");

            // 1. 更新任务状态为 RUNNING
            hrEmpExportTaskService.updateRunning(taskId);

            // 2. 查询任务记录，获取请求参数
            HrEmpExportTask task = hrEmpExportTaskService.getByTaskId(taskId);
            if (task == null) {
                log.error("导出任务不存在, taskId={}", taskId);
                hrEmpExportTaskService.updateFailed(taskId, "导出任务不存在", 0);
                return;
            }

            // 3. 解析请求参数
            HrEmpBatchExportRequest request;
            try {
                request = objectMapper.readValue(task.getRequestParam(), HrEmpBatchExportRequest.class);
            } catch (Exception e) {
                log.error("解析导出请求参数失败, taskId={}", taskId, e);
                hrEmpExportTaskService.updateFailed(taskId, "解析导出请求参数失败: " + truncateMessage(e.getMessage()), 0);
                return;
            }

            // 4. 执行导出并上传 MinIO
            // 创建进度更新器
            ExportProgressUpdater progressUpdater = createProgressUpdater(taskId);

            // 导出 Excel 到字节数组（内部会更新进度）
            byte[] excelBytes = hrEmpExportService.exportHrEmpInfo(request, progressUpdater);

            // 更新进度到 95%（MinIO 上传阶段）
            hrEmpExportTaskService.updateProgress(taskId, 95);

            // 5. 上传到 MinIO
            String fileName = "人员信息集-" +
                    new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()) + ".xlsx";
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                    excelBytes, "file", fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            String fileId;
            try {
                fileId = minIOUtils.saveFile(multipartFile);
            } catch (Exception e) {
                log.error("MinIO上传失败, taskId={}", taskId, e);
                hrEmpExportTaskService.updateFailed(taskId, "MinIO上传失败: " + truncateMessage(e.getMessage()), 90);
                return;
            }

            // 6. 更新任务状态为 SUCCESS
            hrEmpExportTaskService.updateSuccess(taskId, fileId, fileName);
            log.info("导出任务执行成功, taskId={}, fileId={}", taskId, fileId);

        } catch (Exception e) {
            log.error("导出任务执行异常, taskId={}", taskId, e);
            // 获取当前进度
            HrEmpExportTask currentTask = hrEmpExportTaskService.getByTaskId(taskId);
            int currentProgress = currentTask != null && currentTask.getProgress() != null ? currentTask.getProgress() : 0;
            hrEmpExportTaskService.updateFailed(taskId, "导出失败: " + truncateMessage(e.getMessage()), currentProgress);
        } finally {
            MultiTenantHandler.tenantId.set(oldTenant);
        }
    }

    /**
     * 创建进度更新器，委托给 HrEmpExportTaskService
     */
    private ExportProgressUpdater createProgressUpdater(String taskId) {
        return new ExportProgressUpdater() {
            private static final int BATCH_THRESHOLD = 100;
            private int accumulatedDelta = 0;

            @Override
            public void initTotal(int totalCount) {
                hrEmpExportTaskService.initTotal(taskId, totalCount);
            }

            @Override
            public void increment(int finishedDelta) {
                accumulatedDelta += finishedDelta;
                // 每累积到 BATCH_THRESHOLD 条才更新一次数据库
                if (accumulatedDelta >= BATCH_THRESHOLD) {
                    hrEmpExportTaskService.incrementFinished(taskId, accumulatedDelta);
                    accumulatedDelta = 0;
                }
            }

            @Override
            public void updateFinished(int finishedCount) {
                hrEmpExportTaskService.updateFinished(taskId, finishedCount);
                accumulatedDelta = 0;
            }

            @Override
            public void updateProgress(int progress) {
                hrEmpExportTaskService.updateProgress(taskId, progress);
            }
        };
    }

    /**
     * 截断异常信息，避免返回过长堆栈给前端
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "未知错误";
        }
        // 只保留第一行或前500个字符
        int newlineIdx = message.indexOf('\n');
        String firstLine = newlineIdx > 0 ? message.substring(0, newlineIdx) : message;
        if (firstLine.length() > 500) {
            return firstLine.substring(0, 500) + "...";
        }
        return firstLine;
    }
}
