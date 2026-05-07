package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人事信息异步导出任务表
 */
@Data
@TableName("hr_emp_export_task")
public class HrEmpExportTask extends BaseEntity {

    @TableId("id")
    private String id;

    @TableField("task_id")
    private String taskId;

    @TableField("task_type")
    private String taskType;

    @TableField("status")
    private String status;

    @TableField("progress")
    private Integer progress;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("finished_count")
    private Integer finishedCount;

    @TableField("file_id")
    private String fileId;

    @TableField("file_name")
    private String fileName;

    @TableField("request_param")
    private String requestParam;

    @TableField("error_message")
    private String errorMessage;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry_count")
    private Integer maxRetryCount;

    @TableField("cancel_flag")
    private String cancelFlag;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableLogic(value = "0")
    @TableField("deleted")
    private Integer deleted;
}
