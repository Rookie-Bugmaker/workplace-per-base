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
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 薪资标准
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_salary_standard")
@Schema(description = "薪资标准")
@Data
@NoArgsConstructor
public class HrEmpSalaryStandard extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪酬矩阵"}, index = 0)
    @Schema(description = "薪酬矩阵")
    private String salaryMatrix;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗薪档级"}, index = 1)
    @Schema(description = "岗薪档级")
    private String postSalaryGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪档"}, index = 2)
    @Schema(description = "薪档")
    private String salaryGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪级"}, index = 3)
    @Schema(description = "薪级")
    private String salaryLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"执行系数/金额"}, index = 4)
    @Schema(description = "执行系数/金额")
    private String coefficient;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"生效日期"}, index = 5)
    @Schema(description = "生效日期")
    private LocalDate effectiveDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"备注"}, index = 6)
    @Schema(description = "备注")
    private String remarks;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 8)
    @Schema(description = "工号")
    private String userCode;

}
