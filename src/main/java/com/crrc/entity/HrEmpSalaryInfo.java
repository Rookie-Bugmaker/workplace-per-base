package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 员工薪资信息表
 * </p>
 *
 * @author why
 * @since 2025-08-26 15:23:52
 */
@ExcelIgnoreUnannotated
@TableName("hr_emp_salary_info")
@Schema(description = "员工薪资信息表")
@Data
public class HrEmpSalaryInfo extends BaseEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案号"}, index = 0)
    @Schema(description = "档案号", example = "")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"}, index = 1)
    @Schema(description = "姓名", example = "")
    private String name;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工基本工资"}, index = 2)
    @Schema(description = "员工基本工资", example = "")
    private String baseSalary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗位对应的工资"}, index = 3)
    @Schema(description = "岗位对应的工资", example = "")
    private String positionSalary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"费用列支单位编号"}, index = 4)
    @Schema(description = "费用列支单位编号", example = "")
    private String departmentCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才特殊补贴"}, index = 5)
    @Schema(description = "人才特殊补贴", example = "")
    private String talentAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"技师技能补贴"}, index = 6)
    @Schema(description = "技师技能补贴", example = "")
    private String technicianAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工龄对应的补贴标准"}, index = 7)
    @Schema(description = "工龄对应的补贴标准", example = "")
    private String seniorityStandard;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"外语能力补贴"}, index = 8)
    @Schema(description = "外语能力补贴", example = "")
    private String languageAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"车辆相关补贴"}, index = 9)
    @Schema(description = "车辆相关补贴", example = "")
    private String vehicleAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"伤残补助"}, index = 10)
    @Schema(description = "伤残补助", example = "")
    private String disabilityAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类(第四级)"}, index = 11)
    @Schema(description = "人员分类(第四级)", example = "")
    private String employeeCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才通道等级"}, index = 12)
    @Schema(description = "人才通道等级", example = "")
    private String talentLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工资标准基数"}, index = 13)
    @Schema(description = "工资标准基数", example = "")
    private String salaryStandard;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才岗位差额补贴"}, index = 14)
    @Schema(description = "人才岗位差额补贴", example = "")
    private String talentPositionDiff;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"月度绩效系数"}, index = 15)
    @Schema(description = "月度绩效系数", example = "")
    private String monthlyCoefficient;
}
