package com.crrc.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 职位说明书审批请求。
 */
@Data
@Schema(description = "职位说明书审批请求")
public class PositionDescriptionAuditDTO {
    @Schema(description = "职位说明书ID")
    private Long id;

    @Schema(description = "Flowable流程实例ID；为空时根据ID读取")
    private String procInstId;

    @Schema(description = "Flowable任务ID，前端有则回传")
    private String taskId;

    @Schema(description = "节点Key，前端有则回传")
    private String nodeKey;

    @Schema(description = "节点名称，前端有则回传")
    private String nodeName;

    @Schema(description = "角色编码，前端有则回传")
    private String roleCode;

    @Schema(description = "审批人工号；为空时取当前登录人工号")
    private String auditorCode;

    @Schema(description = "审批人姓名；为空时取当前登录人名称")
    private String auditorName;

    @Schema(description = "审批意见")
    private String auditOpinion;

    @Schema(description = "审批时需要重置到 Flowable 的流程变量，可选")
    private Map<String, Object> variables;
}
