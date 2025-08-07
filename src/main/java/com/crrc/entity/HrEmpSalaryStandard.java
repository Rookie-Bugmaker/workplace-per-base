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
import lombok.experimental.Accessors;

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
public class HrEmpSalaryStandard extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021420111196160L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;


    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪酬矩阵"}, index = 0)
    @Schema(description = "薪酬矩阵", example = "")
    private String salaryMatrix;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗薪档级"}, index = 1)
    @Schema(description = "岗薪档级", example = "")
    private String postSalaryGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪档"}, index = 2)
    @Schema(description = "薪档", example = "")
    private String salaryGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪级"}, index = 3)
    @Schema(description = "薪级", example = "")
    private String slaaryLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"执行系数/金额"}, index = 4)
    @Schema(description = "执行系数/金额", example = "")
    private String coefficient;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "effectiveDateStartTime", end = "effectiveDateEndTime")
    @ExcelProperty(value = {"生效日期"}, index = 5)
    @Schema(description = "生效日期", example = "")
    private LocalDateTime effectiveDate;
    
  	@TableField(exist = false)
    @Schema(description = "生效日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime effectiveDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "生效日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime effectiveDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"备注"}, index = 6)
    @Schema(description = "备注", example = "")
    private String remarks;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 8)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    public HrEmpSalaryStandard() {
    }

	public void setSalaryMatrix(String salaryMatrix) {
		this.salaryMatrix = salaryMatrix;
	}
	
	public String getSalaryMatrix() {
		return this.salaryMatrix;
	}
    
	public void setPostSalaryGrade(String postSalaryGrade) {
		this.postSalaryGrade = postSalaryGrade;
	}
	
	public String getPostSalaryGrade() {
		return this.postSalaryGrade;
	}
    
	public void setSalaryGrade(String salaryGrade) {
		this.salaryGrade = salaryGrade;
	}
	
	public String getSalaryGrade() {
		return this.salaryGrade;
	}
    
	public void setSlaaryLevel(String slaaryLevel) {
		this.slaaryLevel = slaaryLevel;
	}
	
	public String getSlaaryLevel() {
		return this.slaaryLevel;
	}
    
	public void setCoefficient(String coefficient) {
		this.coefficient = coefficient;
	}
	
	public String getCoefficient() {
		return this.coefficient;
	}
    
	public void setEffectiveDate(LocalDateTime effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	public LocalDateTime getEffectiveDate() {
		return this.effectiveDate;
	}
    
	public void setEffectiveDateStartTime(LocalDateTime effectiveDateStartTime) {
		this.effectiveDateStartTime = effectiveDateStartTime;
	}
	
	public LocalDateTime getEffectiveDateStartTime() {
		return this.effectiveDateStartTime;
	}
    
    public void setEffectiveDateEndTime(LocalDateTime effectiveDateEndTime) {
		this.effectiveDateEndTime = effectiveDateEndTime;
	}
	
	public LocalDateTime getEffectiveDateEndTime() {
		return this.effectiveDateEndTime;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public String getRemarks() {
		return this.remarks;
	}
    
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
    
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	
	public String getUserCode() {
		return this.userCode;
	}
    

    @Override
    public String toString() {
        return "HrEmpSalaryStandard{" +
		"salaryMatrix=" + salaryMatrix +
		",postSalaryGrade='" + postSalaryGrade + "'" + 
		",salaryGrade='" + salaryGrade + "'" + 
		",slaaryLevel='" + slaaryLevel + "'" + 
		",coefficient='" + coefficient + "'" + 
		",effectiveDate='" + effectiveDate + "'" + 
		",remarks='" + remarks + "'" + 
		",id='" + id + "'" + 
		",userCode='" + userCode + "'" + 
        '}';
    }
}
