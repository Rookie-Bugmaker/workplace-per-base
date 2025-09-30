package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工惩处信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_penalties")
@Schema(description = "员工惩处信息表")
@Data
@NoArgsConstructor
public class HrEmpPenalties extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处名称"}, index = 2)
    @Schema(description = "惩处名称")
    private String penaltyName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"批准单位"}, index = 3)
    @Schema(description = "批准单位")
    private String penaltyUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处文件号"}, index = 4)
    @Schema(description = "惩处文件号")
    private String penaltyDocId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处原因"}, index = 5)
    @Schema(description = "惩处原因")
    private String penaltyReason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处措施"}, index = 6)
    @Schema(description = "惩处措施")
    private String penaltyMeasures;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"}, index = 7)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;
}
