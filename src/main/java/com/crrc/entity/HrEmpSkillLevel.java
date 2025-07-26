package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.Accessors;

/**
 * <p>
 * 职业技能等级
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_skill_level")
@Schema(description = "职业技能等级")
public class HrEmpSkillLevel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419805011968L;

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

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工种"}, index = 0)
    @Schema(description = "工种", example = "")
    private String jobId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"技能等级"}, index = 1)
    @Schema(description = "技能等级", example = "")
    private String skillLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得方式"}, index = 2)
    @Schema(description = "取得方式", example = "")
    private String getMethod;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "getDateStartTime", end = "getDateEndTime")
    @ExcelProperty(value = {"取得时间"}, index = 3)
    @Schema(description = "取得时间", example = "")
    private LocalDateTime getDate;
    
  	@TableField(exist = false)
    @Schema(description = "取得时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime getDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "取得时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime getDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高"}, index = 4)
    @Schema(description = "是否最高", example = "")
    private String isHighest;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否聘任"}, index = 5)
    @Schema(description = "是否聘任", example = "")
    private String isAppointed;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "appointedStartDateStartTime", end = "appointedStartDateEndTime")
    @ExcelProperty(value = {"聘任起始时间"}, index = 6)
    @Schema(description = "聘任起始时间", example = "")
    private LocalDateTime appointedStartDate;
    
  	@TableField(exist = false)
    @Schema(description = "聘任起始时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime appointedStartDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "聘任起始时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime appointedStartDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "appointedEndDateStartTime", end = "appointedEndDateEndTime")
    @ExcelProperty(value = {"聘任结束时间"}, index = 7)
    @Schema(description = "聘任结束时间", example = "")
    private LocalDateTime appointedEndDate;
    
  	@TableField(exist = false)
    @Schema(description = "聘任结束时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime appointedEndDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "聘任结束时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime appointedEndDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"发证机构"}, index = 8)
    @Schema(description = "发证机构", example = "")
    private String grantUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书编号"}, index = 9)
    @Schema(description = "证书编号", example = "")
    private String certiId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 11)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    public HrEmpSkillLevel() {
    }

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getJobId() {
		return this.jobId;
	}
    
	public void setSkillLevel(String skillLevel) {
		this.skillLevel = skillLevel;
	}
	
	public String getSkillLevel() {
		return this.skillLevel;
	}
    
	public void setGetMethod(String getMethod) {
		this.getMethod = getMethod;
	}
	
	public String getGetMethod() {
		return this.getMethod;
	}
    
	public void setGetDate(LocalDateTime getDate) {
		this.getDate = getDate;
	}
	
	public LocalDateTime getGetDate() {
		return this.getDate;
	}
    
	public void setGetDateStartTime(LocalDateTime getDateStartTime) {
		this.getDateStartTime = getDateStartTime;
	}
	
	public LocalDateTime getGetDateStartTime() {
		return this.getDateStartTime;
	}
    
    public void setGetDateEndTime(LocalDateTime getDateEndTime) {
		this.getDateEndTime = getDateEndTime;
	}
	
	public LocalDateTime getGetDateEndTime() {
		return this.getDateEndTime;
	}

	public void setIsHighest(String isHighest) {
		this.isHighest = isHighest;
	}
	
	public String getIsHighest() {
		return this.isHighest;
	}
    
	public void setIsAppointed(String isAppointed) {
		this.isAppointed = isAppointed;
	}
	
	public String getIsAppointed() {
		return this.isAppointed;
	}
    
	public void setAppointedStartDate(LocalDateTime appointedStartDate) {
		this.appointedStartDate = appointedStartDate;
	}
	
	public LocalDateTime getAppointedStartDate() {
		return this.appointedStartDate;
	}
    
	public void setAppointedStartDateStartTime(LocalDateTime appointedStartDateStartTime) {
		this.appointedStartDateStartTime = appointedStartDateStartTime;
	}
	
	public LocalDateTime getAppointedStartDateStartTime() {
		return this.appointedStartDateStartTime;
	}
    
    public void setAppointedStartDateEndTime(LocalDateTime appointedStartDateEndTime) {
		this.appointedStartDateEndTime = appointedStartDateEndTime;
	}
	
	public LocalDateTime getAppointedStartDateEndTime() {
		return this.appointedStartDateEndTime;
	}

	public void setAppointedEndDate(LocalDateTime appointedEndDate) {
		this.appointedEndDate = appointedEndDate;
	}
	
	public LocalDateTime getAppointedEndDate() {
		return this.appointedEndDate;
	}
    
	public void setAppointedEndDateStartTime(LocalDateTime appointedEndDateStartTime) {
		this.appointedEndDateStartTime = appointedEndDateStartTime;
	}
	
	public LocalDateTime getAppointedEndDateStartTime() {
		return this.appointedEndDateStartTime;
	}
    
    public void setAppointedEndDateEndTime(LocalDateTime appointedEndDateEndTime) {
		this.appointedEndDateEndTime = appointedEndDateEndTime;
	}
	
	public LocalDateTime getAppointedEndDateEndTime() {
		return this.appointedEndDateEndTime;
	}

	public void setGrantUnit(String grantUnit) {
		this.grantUnit = grantUnit;
	}
	
	public String getGrantUnit() {
		return this.grantUnit;
	}
    
	public void setCertiId(String certiId) {
		this.certiId = certiId;
	}
	
	public String getCertiId() {
		return this.certiId;
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
        return "HrEmpSkillLevel{" +
		"jobId=" + jobId +
		",skillLevel='" + skillLevel + "'" + 
		",getMethod='" + getMethod + "'" + 
		",getDate='" + getDate + "'" + 
		",isHighest='" + isHighest + "'" + 
		",isAppointed='" + isAppointed + "'" + 
		",appointedStartDate='" + appointedStartDate + "'" + 
		",appointedEndDate='" + appointedEndDate + "'" + 
		",gaintUnit='" + grantUnit + "'" +
		",certiId='" + certiId + "'" + 
		",id='" + id + "'" + 
		",userCode='" + userCode + "'" + 
        '}';
    }
}
