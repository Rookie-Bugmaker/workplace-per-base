package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 职位管理模块菜单按钮与接口对照。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "职位管理模块菜单按钮与接口对照")
public class PositionApiEndpointVO {
    @Schema(description = "模块")
    private String module;

    @Schema(description = "按钮/功能")
    private String action;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "接口路径")
    private String path;

    @Schema(description = "接口说明")
    private String description;

    @Schema(description = "新增/修改")
    private String changeType;

    @Schema(description = "请求说明")
    private String requestRemark;

    @Schema(description = "返回说明")
    private String responseRemark;
}
