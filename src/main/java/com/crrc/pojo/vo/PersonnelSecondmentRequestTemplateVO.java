package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.annotation.WordTemplateRequire;
import com.crrc.entity.BaseEntity;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

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
public class PersonnelSecondmentRequestTemplateVO extends BaseEntity {
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"序号"})
    @Schema(description = "序号")
    @WordTemplateRequire(index = 0)
    private int index;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工种"})
    @Schema(description = "工种")
    @WordTemplateRequire(index = 1)
    private String jobCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资质"})
    @Schema(description = "资质")
    @WordTemplateRequire(index = 2)
    private String qualificationRequirements;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求数量"})
    @Schema(description = "需求数量")
    @WordTemplateRequire(index = 3)
    private Integer requiredQuantity;

    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"起止时间"})
    @Schema(description = "起止时间")
    @WordTemplateRequire(index = 4)
    private String startEndDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"建议借出单位"})
    @Schema(description = "建议借出单位")
    @WordTemplateRequire(index = 5)
    private String suggestedSourceDepartment;
}
