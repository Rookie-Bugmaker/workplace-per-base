package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员审批表
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@ExcelIgnoreUnannotated
@TableName("borrowed_personnel_approval_form_relation")
@Schema(description = "借调（借用）人员审批表")
@Data
public class BorrowedPersonnelApprovalFormRelation extends BaseEntity{

    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借入单位"})
    @Schema(description = "借入单位")
    private String borrowingUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借入单位名称"})
    @Schema(description = "借入单位名称")
    private String borrowingUnitName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借出单位"})
    @Schema(description = "借出单位")
    private String lendingUnit;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借出单位名称"})
    @Schema(description = "借出单位名称")
    private String lendingUnitName;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"开始时间"})
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束时间"})
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"作业范围与要求"})
    @Schema(description = "作业范围与要求")
    private String workScopeRequirements;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结算方式"})
    @Schema(description = "结算方式")
    private String settlementMethod;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程模板id"})
    @Schema(description = "流程模板id")
    private String wfId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程实例id"})
    @Schema(description = "流程实例id")
    private String processInstanceId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"审批状态"})
    @Schema(description = "审批状态")
    private String orderStatus;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"文件id"})
    @Schema(description = "文件id")
    private String fileId;
}
