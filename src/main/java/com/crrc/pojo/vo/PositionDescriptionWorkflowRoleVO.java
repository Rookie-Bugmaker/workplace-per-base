package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 职位说明书工作流角色配置视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "职位说明书工作流角色配置")
public class PositionDescriptionWorkflowRoleVO {
    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "角色编码")
    private String roleCode;
}
