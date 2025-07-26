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
 * 员工专业职务任职资格表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_professional_qualification")
@Schema(description = "员工专业职务任职资格表")
public class HrEmpProfessionalQualification extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419159089152L;

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
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业技术资格"}, index = 2)
    @Schema(description = "专业技术资格", example = "")
    private String quaId;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "quaDateStartTime", end = "quaDateEndTime")
    @ExcelProperty(value = {"获得资格时间"}, index = 3)
    @Schema(description = "获得资格时间", example = "")
    private LocalDateTime quaDate;
    
  	@TableField(exist = false)
    @Schema(description = "获得资格时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime quaDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "获得资格时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime quaDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职称级别"}, index = 4)
    @Schema(description = "职称级别", example = "")
    private String quaTitleLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资格审批单位"}, index = 5)
    @Schema(description = "资格审批单位", example = "")
    private String quaApprovalUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得资格途径"}, index = 6)
    @Schema(description = "取得资格途径", example = "")
    private String quaPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高"}, index = 7)
    @Schema(description = "是否最高", example = "")
    private String isHighest;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否聘任"}, index = 8)
    @Schema(description = "是否聘任", example = "")
    private String isAppointed;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "appointedStartDateStartTime", end = "appointedStartDateEndTime")
    @ExcelProperty(value = {"聘任起始时间"}, index = 9)
    @Schema(description = "聘任起始时间", example = "")
    private LocalDateTime appointedStartDate;
    
  	@TableField(exist = false)
    @Schema(description = "聘任起始时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime appointedStartDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "聘任起始时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime appointedStartDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "appointedEndDateStartTime", end = "appointedEndDateEndTime")
    @ExcelProperty(value = {"聘任结束时间"}, index = 10)
    @Schema(description = "聘任结束时间", example = "")
    private LocalDateTime appointedEndDate;
    
  	@TableField(exist = false)
    @Schema(description = "聘任结束时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime appointedEndDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "聘任结束时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime appointedEndDateEndTime;
    
    public HrEmpProfessionalQualification() {
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
    
	public void setQuaId(String quaId) {
		this.quaId = quaId;
	}
	
	public String getQuaId() {
		return this.quaId;
	}
    
	public void setQuaDate(LocalDateTime quaDate) {
		this.quaDate = quaDate;
	}
	
	public LocalDateTime getQuaDate() {
		return this.quaDate;
	}
    
	public void setQuaDateStartTime(LocalDateTime quaDateStartTime) {
		this.quaDateStartTime = quaDateStartTime;
	}
	
	public LocalDateTime getQuaDateStartTime() {
		return this.quaDateStartTime;
	}
    
    public void setQuaDateEndTime(LocalDateTime quaDateEndTime) {
		this.quaDateEndTime = quaDateEndTime;
	}
	
	public LocalDateTime getQuaDateEndTime() {
		return this.quaDateEndTime;
	}

	public void setQuaTitleLevel(String quaTitleLevel) {
		this.quaTitleLevel = quaTitleLevel;
	}
	
	public String getQuaTitleLevel() {
		return this.quaTitleLevel;
	}
    
	public void setQuaApprovalUnit(String quaApprovalUnit) {
		this.quaApprovalUnit = quaApprovalUnit;
	}
	
	public String getQuaApprovalUnit() {
		return this.quaApprovalUnit;
	}
    
	public void setQuaPath(String quaPath) {
		this.quaPath = quaPath;
	}
	
	public String getQuaPath() {
		return this.quaPath;
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


    @Override
    public String toString() {
        return "HrEmpProfessionalQualification{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",quaId='" + quaId + "'" + 
		",quaDate='" + quaDate + "'" + 
		",quaTitleLevel='" + quaTitleLevel + "'" + 
		",quaApprovalUnit='" + quaApprovalUnit + "'" + 
		",quaPath='" + quaPath + "'" + 
		",isHighest='" + isHighest + "'" + 
		",isAppointed='" + isAppointed + "'" + 
		",appointedStartDate='" + appointedStartDate + "'" + 
		",appointedEndDate='" + appointedEndDate + "'" + 
        '}';
    }
}
