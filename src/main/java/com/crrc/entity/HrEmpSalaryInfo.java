package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

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
@Accessors(chain = false)
public class HrEmpSalaryInfo extends BaseEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = "档案号", index = 0)
    @Schema(description = "档案号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"})
    @Schema(description = "姓名")
    private String name;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工基本工资"})
    @Schema(description = "员工基本工资")
    private String baseSalary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗位对应的工资"})
    @Schema(description = "岗位对应的工资")
    private String positionSalary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = "费用列支单位编号", index = 1)
    @Schema(description = "费用列支单位编号")
    private String departmentCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才特殊补贴"})
    @Schema(description = "人才特殊补贴")
    private String talentAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"技师技能补贴"})
    @Schema(description = "技师技能补贴")
    private String technicianAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工龄对应的补贴标准"})
    @Schema(description = "工龄对应的补贴标准")
    private String seniorityStandard;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"外语能力补贴"})
    @Schema(description = "外语能力补贴")
    private String languageAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"车辆相关补贴"})
    @Schema(description = "车辆相关补贴")
    private String vehicleAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"伤残补助"})
    @Schema(description = "伤残补助")
    private String disabilityAllowance;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类(第四级)"})
    @Schema(description = "人员分类(第四级)")
    private String employeeCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才通道等级"})
    @Schema(description = "人才通道等级")
    private String talentLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工资标准基数"})
    @Schema(description = "工资标准基数")
    private String salaryStandard;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人才岗位差额补贴"})
    @Schema(description = "人才岗位差额补贴")
    private String talentPositionDiff;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"月度绩效系数"})
    @Schema(description = "月度绩效系数")
    private String monthlyCoefficient;
}
