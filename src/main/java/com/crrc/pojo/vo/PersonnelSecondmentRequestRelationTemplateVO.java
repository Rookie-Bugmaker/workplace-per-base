package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.annotation.WordTemplateRequire;
import com.crrc.entity.BaseEntity;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@ExcelIgnoreUnannotated
@TableName("personnel_secondment_request_relation")
@Schema(description = "借调（借用）人员需求计划申请表表头")
@Data
public class PersonnelSecondmentRequestRelationTemplateVO extends BaseEntity {

    @WordTemplateRequire
    private String year;

    @WordTemplateRequire
    private String month;

    @WordTemplateRequire
    private String day;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求单位"})
    @Schema(description = "需求单位")
    @WordTemplateRequire
    private String requestingDepartment;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求项目"})
    @Schema(description = "需求项目")
    @WordTemplateRequire
    private String projectName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求事由描述"})
    @Schema(description = "需求事由描述")
    @WordTemplateRequire
    private String reasonDescription;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作地点"})
    @Schema(description = "工作地点")
    @WordTemplateRequire
    private String workLocation;
}
