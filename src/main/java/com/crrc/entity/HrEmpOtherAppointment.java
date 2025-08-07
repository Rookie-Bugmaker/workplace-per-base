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
 * 其他任职信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_other_appointment")
@Schema(description = "其他任职信息")
public class HrEmpOtherAppointment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021420736147456L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;


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
    
    @QueryMethod(method = QueryType.BETWEEN, start = "expectedEndDateStartTime", end = "expectedEndDateEndTime")
    @ExcelProperty(value = {"预计结束日期"}, index = 1)
    @Schema(description = "预计结束日期", example = "")
    private LocalDateTime expectedEndDate;
    
  	@TableField(exist = false)
    @Schema(description = "预计结束日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime expectedEndDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "预计结束日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime expectedEndDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "endDateStartTime", end = "endDateEndTime")
    @ExcelProperty(value = {"结束日期"}, index = 2)
    @Schema(description = "结束日期", example = "")
    private LocalDateTime endDate;
    
  	@TableField(exist = false)
    @Schema(description = "结束日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime endDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "结束日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime endDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"}, index = 3)
    @Schema(description = "人员类别", example = "")
    private String personnelCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织"}, index = 4)
    @Schema(description = "组织", example = "")
    private String org;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"}, index = 5)
    @Schema(description = "部门", example = "")
    private String dept;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织单元"}, index = 6)
    @Schema(description = "组织单元", example = "")
    private String orgUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案关系弱化"}, index = 7)
    @Schema(description = "档案关系弱化", example = "")
    private String archiveRelationWeaken;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"}, index = 8)
    @Schema(description = "职位", example = "")
    private String position;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"其他任职类型"}, index = 9)
    @Schema(description = "其他任职类型", example = "")
    private String otherAppointmentType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"集团外任职信息"}, index = 10)
    @Schema(description = "集团外任职信息", example = "")
    private String groupExternalAppointmentInfo;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务类别"}, index = 11)
    @Schema(description = "职务类别", example = "")
    private String positionCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"在岗状态"}, index = 12)
    @Schema(description = "在岗状态", example = "")
    private String dutyStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织路径"}, index = 13)
    @Schema(description = "组织路径", example = "")
    private String orgPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门路径"}, index = 14)
    @Schema(description = "部门路径", example = "")
    private String deptPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 16)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    public HrEmpOtherAppointment() {
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

	public void setExpectedEndDate(LocalDateTime expectedEndDate) {
		this.expectedEndDate = expectedEndDate;
	}
	
	public LocalDateTime getExpectedEndDate() {
		return this.expectedEndDate;
	}
    
	public void setExpectedEndDateStartTime(LocalDateTime expectedEndDateStartTime) {
		this.expectedEndDateStartTime = expectedEndDateStartTime;
	}
	
	public LocalDateTime getExpectedEndDateStartTime() {
		return this.expectedEndDateStartTime;
	}
    
    public void setExpectedEndDateEndTime(LocalDateTime expectedEndDateEndTime) {
		this.expectedEndDateEndTime = expectedEndDateEndTime;
	}
	
	public LocalDateTime getExpectedEndDateEndTime() {
		return this.expectedEndDateEndTime;
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

	public void setPersonnelCategory(String personnelCategory) {
		this.personnelCategory = personnelCategory;
	}
	
	public String getPersonnelCategory() {
		return this.personnelCategory;
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
    
	public void setArchiveRelationWeaken(String archiveRelationWeaken) {
		this.archiveRelationWeaken = archiveRelationWeaken;
	}
	
	public String getArchiveRelationWeaken() {
		return this.archiveRelationWeaken;
	}
    
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return this.position;
	}
    
	public void setOtherAppointmentType(String otherAppointmentType) {
		this.otherAppointmentType = otherAppointmentType;
	}
	
	public String getOtherAppointmentType() {
		return this.otherAppointmentType;
	}
    
	public void setGroupExternalAppointmentInfo(String groupExternalAppointmentInfo) {
		this.groupExternalAppointmentInfo = groupExternalAppointmentInfo;
	}
	
	public String getGroupExternalAppointmentInfo() {
		return this.groupExternalAppointmentInfo;
	}
    
	public void setPositionCategory(String positionCategory) {
		this.positionCategory = positionCategory;
	}
	
	public String getPositionCategory() {
		return this.positionCategory;
	}
    
	public void setDutyStatus(String dutyStatus) {
		this.dutyStatus = dutyStatus;
	}
	
	public String getDutyStatus() {
		return this.dutyStatus;
	}
    
	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}
	
	public String getOrgPath() {
		return this.orgPath;
	}
    
	public void setDeptPath(String deptPath) {
		this.deptPath = deptPath;
	}
	
	public String getDeptPath() {
		return this.deptPath;
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
        return "HrEmpOtherAppointment{" +
		"startDate=" + startDate +
		",expectedEndDate='" + expectedEndDate + "'" + 
		",endDate='" + endDate + "'" + 
		",personnelCategory='" + personnelCategory + "'" + 
		",org='" + org + "'" + 
		",dept='" + dept + "'" + 
		",orgUnit='" + orgUnit + "'" + 
		",archiveRelationWeaken='" + archiveRelationWeaken + "'" + 
		",position='" + position + "'" + 
		",otherAppointmentType='" + otherAppointmentType + "'" + 
		",groupExternalAppointmentInfo='" + groupExternalAppointmentInfo + "'" + 
		",positionCategory='" + positionCategory + "'" + 
		",dutyStatus='" + dutyStatus + "'" + 
		",orgPath='" + orgPath + "'" + 
		",deptPath='" + deptPath + "'" + 
		",id='" + id + "'" + 
		",userCode='" + userCode + "'" + 
        '}';
    }
}
