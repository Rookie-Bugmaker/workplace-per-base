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
 * 任职信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_appointment")
@Schema(description = "任职信息")
public class HrEmpAppointment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021422007021568L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "备注", example = "demo")
    @QueryMethod(method = QueryType.LIKE)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "所属部门id", example = "1")
    private Long orgId;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.BETWEEN, start = "startDateStartTime", end = "startDateEndTime")
    @ExcelProperty(value = {"开始日期"}, index = 0)
    @Schema(description = "开始日期", example = "")
    private LocalDateTime startDate;
    
  	@TableField(exist = false)
    @Schema(description = "开始日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime startDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "开始日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束日期"}, index = 1)
    @Schema(description = "结束日期", example = "")
    private LocalDateTime endDate;
    
  	@TableField(exist = false)
    @Schema(description = "结束日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime endDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "结束日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime endDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动事件"}, index = 2)
    @Schema(description = "变动事件", example = "")
    private String changeEvent;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动类型"}, index = 3)
    @Schema(description = "变动类型", example = "")
    private String changeType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动原因"}, index = 4)
    @Schema(description = "变动原因", example = "")
    private String changeReason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"}, index = 5)
    @Schema(description = "人员类别", example = "")
    private String personnelCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"在岗状态"}, index = 6)
    @Schema(description = "在岗状态", example = "")
    private String dutyStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织"}, index = 7)
    @Schema(description = "组织", example = "")
    private String org;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"}, index = 8)
    @Schema(description = "部门", example = "")
    private String dept;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织单元"}, index = 9)
    @Schema(description = "组织单元", example = "")
    private String orgUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"}, index = 10)
    @Schema(description = "职位", example = "")
    private String position;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"}, index = 11)
    @Schema(description = "职级层级", example = "")
    private String hierarchyRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务名称"}, index = 12)
    @Schema(description = "职务名称", example = "")
    private String positionName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"}, index = 13)
    @Schema(description = "职务层级", example = "")
    private String positionHierarchy;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务类别"}, index = 14)
    @Schema(description = "职务类别", example = "")
    private String positionCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪资组"}, index = 15)
    @Schema(description = "薪资组", example = "")
    private String salaryGroup;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 17)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    public HrEmpAppointment() {
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

	public void setChangeEvent(String changeEvent) {
		this.changeEvent = changeEvent;
	}
	
	public String getChangeEvent() {
		return this.changeEvent;
	}
    
	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}
	
	public String getChangeType() {
		return this.changeType;
	}
    
	public void setChangeReason(String changeReason) {
		this.changeReason = changeReason;
	}
	
	public String getChangeReason() {
		return this.changeReason;
	}
    
	public void setPersonnelCategory(String personnelCategory) {
		this.personnelCategory = personnelCategory;
	}
	
	public String getPersonnelCategory() {
		return this.personnelCategory;
	}
    
	public void setDutyStatus(String dutyStatus) {
		this.dutyStatus = dutyStatus;
	}
	
	public String getDutyStatus() {
		return this.dutyStatus;
	}
    
	public void setOrg(String org) {
		this.org = org;
	}
	
	public String getOrg() {
		return this.org;
	}
    
	public void setDept(String dept) {
		this.dept = dept;
	}
	
	public String getDept() {
		return this.dept;
	}
    
	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}
	
	public String getOrgUnit() {
		return this.orgUnit;
	}
    
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return this.position;
	}
    
	public void setHierarchyRank(String hierarchyRank) {
		this.hierarchyRank = hierarchyRank;
	}
	
	public String getHierarchyRank() {
		return this.hierarchyRank;
	}
    
	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}
	
	public String getPositionName() {
		return this.positionName;
	}
    
	public void setPositionHierarchy(String positionHierarchy) {
		this.positionHierarchy = positionHierarchy;
	}
	
	public String getPositionHierarchy() {
		return this.positionHierarchy;
	}
    
	public void setPositionCategory(String positionCategory) {
		this.positionCategory = positionCategory;
	}
	
	public String getPositionCategory() {
		return this.positionCategory;
	}
    
	public void setSalaryGroup(String salaryGroup) {
		this.salaryGroup = salaryGroup;
	}
	
	public String getSalaryGroup() {
		return this.salaryGroup;
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
        return "HrEmpAppointment{" +
		"startDate=" + startDate +
		",endDate='" + endDate + "'" + 
		",changeEvent='" + changeEvent + "'" + 
		",changeType='" + changeType + "'" + 
		",changeReason='" + changeReason + "'" + 
		",personnelCategory='" + personnelCategory + "'" + 
		",dutyStatus='" + dutyStatus + "'" + 
		",org='" + org + "'" + 
		",dept='" + dept + "'" + 
		",orgUnit='" + orgUnit + "'" + 
		",position='" + position + "'" + 
		",hierarchyRank='" + hierarchyRank + "'" + 
		",positionName='" + positionName + "'" + 
		",positionHierarchy='" + positionHierarchy + "'" + 
		",positionCategory='" + positionCategory + "'" + 
		",salaryGroup='" + salaryGroup + "'" + 
		",id='" + id + "'" + 
		",userCode='" + userCode + "'" + 
        '}';
    }
}
