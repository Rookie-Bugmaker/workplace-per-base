package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 成本中心表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */

@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_org_cost_center")
@Schema(description ="成本中心表")
@Data
@NoArgsConstructor
@Deprecated
public class HrOrgCostCenter extends BaseEntity implements Serializable {
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "租户id", example = "1")
    private Long tenantId;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"表集合"}, index = 0)
    @Schema(description = "表集合", example = "")
    private String setId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"成本中心编码"}, index = 1)
    @Schema(description = "成本中心编码", example = "")
    private String costCenter;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"成本中心名称"}, index = 2)
    @Schema(description = "成本中心名称", example = "")
    private String costName;
}
