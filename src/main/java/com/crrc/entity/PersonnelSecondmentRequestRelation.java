package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.annotation.WordTemplateRequire;
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
public class PersonnelSecondmentRequestRelation extends BaseEntity{

    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求单位"})
    @Schema(description = "需求单位")
    private String requestingDepartment;

    @TableField(exist = false)
    private String requestingDepartmentValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求项目"})
    @Schema(description = "需求项目")
    private String projectName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求事由描述"})
    @Schema(description = "需求事由描述")
    private String reasonDescription;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作地点"})
    @Schema(description = "工作地点")
    private String workLocation;

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
}
