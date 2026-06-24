package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 职位管理模块前端下拉选项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "职位管理模块前端下拉选项")
public class PositionOptionVO {
    @Schema(description = "选项值")
    private String value;

    @Schema(description = "选项名称")
    private String label;

    @Schema(description = "选项类型")
    private String type;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "备注")
    private String remark;
}
