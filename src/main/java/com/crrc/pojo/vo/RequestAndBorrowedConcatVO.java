package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.crrc.annotation.QueryMethod;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.enums.QueryType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestAndBorrowedConcatVO extends PersonnelSecondmentRequestRelation {
    @Schema(description = "主键")
    @TableId(value = "id")
    private String brwId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借入单位"})
    @Schema(description = "借入单位")
    private String brwBorrowingUnit;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借入单位名称"})
    @Schema(description = "借入单位名称")
    private String brwBorrowingUnitName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借出单位"})
    @Schema(description = "借出单位")
    private String brwLendingUnit;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"借出单位名称"})
    @Schema(description = "借出单位名称")
    private String brwLendingUnitName;

    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"开始时间"})
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate brwStartDate;

    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束时间"})
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate brwEndDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"作业范围与要求"})
    @Schema(description = "作业范围与要求")
    private String brwWorkScopeRequirements;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结算方式"})
    @Schema(description = "结算方式")
    private String brwSettlementMethod;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程模板id"})
    @Schema(description = "流程模板id")
    private String brwWfId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程实例id"})
    @Schema(description = "流程实例id")
    private String brwProcessInstanceId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"审批状态"})
    @Schema(description = "审批状态")
    private String brwOrderStatus;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"文件id"})
    @Schema(description = "文件id")
    private String brwFileId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"对应需求计划id"})
    @Schema(description = "对应需求计划id")
    private String brwRequestId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"目标部门"})
    @Schema(description = "目标部门")
    private String brwTargetDept;
}
