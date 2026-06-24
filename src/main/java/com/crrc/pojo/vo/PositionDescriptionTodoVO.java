package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 当前用户职位说明书审批待办。
 */
@Data
@Schema(description = "当前用户职位说明书审批待办")
public class PositionDescriptionTodoVO {
    @Schema(description = "Flowable任务ID")
    private String taskId;

    @Schema(description = "流程实例ID")
    private String procInstId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务定义Key")
    private String taskDefinitionKey;

    @Schema(description = "任务创建时间")
    private Date taskCreateTime;

    @Schema(description = "Flowable业务ID")
    private String bizId;

    @Schema(description = "Flowable业务编码")
    private String bizCode;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "职位编码")
    private String positionCode;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "所属组织单元")
    private String orgUnit;

    @Schema(description = "职种")
    private String jobType;

    @Schema(description = "说明书状态")
    private String status;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "是否可审批通过")
    private Boolean canApprove;

    @Schema(description = "是否可驳回")
    private Boolean canReject;
}
