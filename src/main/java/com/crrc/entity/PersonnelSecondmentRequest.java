package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员需求计划申请明细表
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@ExcelIgnoreUnannotated
@TableName("personnel_secondment_request")
@Schema(description = "借调（借用）人员需求计划申请明细表")
@Data
public class PersonnelSecondmentRequest extends BaseEntity{
    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"数据外键，对应personnel_secondment_request_relation的id"})
    @Schema(description = "数据外键，对应personnel_secondment_request_relation的id")
    private String relationId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工种"})
    @Schema(description = "工种")
    private String jobCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资质"})
    @Schema(description = "资质")
    private String qualificationRequirements;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求数量"})
    @Schema(description = "需求数量")
    private Integer requiredQuantity;

    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"开始时间"})
    @Schema(description = "开始时间")
    private LocalDate startDate;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束时间"})
    @Schema(description = "结束时间")
    private LocalDate endDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"建议借出单位"})
    @Schema(description = "建议借出单位")
    private String suggestedSourceDepartment;
}
