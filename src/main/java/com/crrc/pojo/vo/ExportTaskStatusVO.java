package com.crrc.pojo.vo;

import lombok.Data;

/**
 * 导出任务状态查询VO
 */
@Data
public class ExportTaskStatusVO {
    private String taskId;
    private String status;
    private Integer progress;
    private Integer finishedCount;
    private Integer totalCount;
    private String fileId;
    private String fileName;
    private String errorMessage;
}
