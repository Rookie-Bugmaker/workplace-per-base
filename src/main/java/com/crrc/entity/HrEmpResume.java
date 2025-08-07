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
 * 员工履历信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_resume")
@Schema(description = "员工履历信息表")
public class HrEmpResume extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021421050720256L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;


    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"开始日期"}, index = 2)
    @Schema(description = "开始日期", example = "")
    private LocalDateTime startDate;
    
  	@TableField(exist = false)
    @Schema(description = "开始日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime startDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "开始日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束日期"}, index = 3)
    @Schema(description = "结束日期", example = "")
    private LocalDateTime endDate;
    
  	@TableField(exist = false)
    @Schema(description = "结束日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime endDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "结束日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime endDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作单位"}, index = 4)
    @Schema(description = "工作单位", example = "")
    private String unit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作部门"}, index = 5)
    @Schema(description = "工作部门", example = "")
    private String department;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"}, index = 6)
    @Schema(description = "职位", example = "")
    private String position;
    
    public HrEmpResume() {
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
    
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	
	public LocalDateTime getStartDate() {
		return this.startDate;
	}
    
	public void setStartDateStartTime(LocalDateTime startDateStartTime) {
		this.startDateStartTime = startDateStartTime;
	}
	
	public LocalDateTime getStartDateStartTime() {
		return this.startDateStartTime;
	}
    
    public void setStartDateEndTime(LocalDateTime startDateEndTime) {
		this.startDateEndTime = startDateEndTime;
	}
	
	public LocalDateTime getStartDateEndTime() {
		return this.startDateEndTime;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	
	public LocalDateTime getEndDate() {
		return this.endDate;
	}
    
	public void setEndDateStartTime(LocalDateTime endDateStartTime) {
		this.endDateStartTime = endDateStartTime;
	}
	
	public LocalDateTime getEndDateStartTime() {
		return this.endDateStartTime;
	}
    
    public void setEndDateEndTime(LocalDateTime endDateEndTime) {
		this.endDateEndTime = endDateEndTime;
	}
	
	public LocalDateTime getEndDateEndTime() {
		return this.endDateEndTime;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String getUnit() {
		return this.unit;
	}
    
	public void setDepartment(String department) {
		this.department = department;
	}
	
	public String getDepartment() {
		return this.department;
	}
    
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return this.position;
	}
    

    @Override
    public String toString() {
        return "HrEmpResume{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",startDate='" + startDate + "'" + 
		",endDate='" + endDate + "'" + 
		",unit='" + unit + "'" + 
		",department='" + department + "'" + 
		",position='" + position + "'" + 
        '}';
    }
}
