package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 职位管理模块首页统计概览。
 */
@Data
@Schema(description = "职位管理模块首页统计概览")
public class PositionManagementOverviewVO {
    @Schema(description = "职位总数")
    private Long positionTotal;

    @Schema(description = "启用职位数")
    private Long positionEnabled;

    @Schema(description = "停用职位数")
    private Long positionDisabled;

    @Schema(description = "职务族总数")
    private Long catalogTotal;

    @Schema(description = "职务族职类数量")
    private Long catalogCategoryCount;

    @Schema(description = "职务族职群数量")
    private Long catalogGroupCount;

    @Schema(description = "职务族职种数量")
    private Long catalogTypeCount;

    @Schema(description = "停用职务族数量")
    private Long catalogDisabled;

    @Schema(description = "职位说明书总数")
    private Long descriptionTotal;

    @Schema(description = "职位说明书各状态数量")
    private Map<String, Long> descriptionStatusCount;
}
