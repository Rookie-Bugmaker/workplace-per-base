package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 职位说明书审批记录表。
 */
@Data
@TableName("position_description_audit_record")
@Accessors(chain = false)
@Schema(description = "职位说明书审批记录")
@NoArgsConstructor
public class PositionDescriptionAuditRecord extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "Flowable流程实例ID")
    private String procInstId;

    @Schema(description = "Flowable任务ID")
    private String taskId;

    @Schema(description = "节点Key")
    private String nodeKey;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "审批人工号")
    private String auditorCode;

    @Schema(description = "审批人姓名")
    private String auditorName;

    @Schema(description = "审批结果：APPROVE通过，REJECT驳回")
    private String auditResult;

    @Schema(description = "审批意见")
    private String auditOpinion;

    @Schema(description = "审批时间")
    private LocalDateTime auditTime;
}
