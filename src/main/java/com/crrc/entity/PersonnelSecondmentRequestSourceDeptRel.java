package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@ExcelIgnoreUnannotated
@TableName("personnel_secondment_request_source_dept_rel")
@Schema(description = "需求计划与实际借出单位关系表")
@Data
public class PersonnelSecondmentRequestSourceDeptRel extends BaseEntity {
    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"需求计划表主键"})
    @Schema(description = "需求计划表主键")
    private String relationId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门编码"})
    @Schema(description = "部门编码")
    private String deptCode;
}
