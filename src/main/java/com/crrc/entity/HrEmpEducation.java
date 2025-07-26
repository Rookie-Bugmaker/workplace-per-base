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
 * 员工教育信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_education")
@Schema(description = "员工教育信息表")
public class HrEmpEducation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021423575691264L;

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
    
    @QueryMethod(method = QueryType.BETWEEN, start = "schoolEntryDateStartTime", end = "schoolEntryDateEndTime")
    @ExcelProperty(value = {"入学时间"}, index = 2)
    @Schema(description = "入学时间", example = "")
    private LocalDateTime schoolEntryDate;
    
  	@TableField(exist = false)
    @Schema(description = "入学时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime schoolEntryDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "入学时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime schoolEntryDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "graduationDateStartTime", end = "graduationDateEndTime")
    @ExcelProperty(value = {"毕业时间"}, index = 3)
    @Schema(description = "毕业时间", example = "")
    private LocalDateTime graduationDate;
    
  	@TableField(exist = false)
    @Schema(description = "毕业时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime graduationDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "毕业时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime graduationDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校名称"}, index = 4)
    @Schema(description = "学校名称", example = "")
    private String schoolName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业"}, index = 5)
    @Schema(description = "专业", example = "")
    private String major;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学院"}, index = 6)
    @Schema(description = "学院", example = "")
    private String college;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否主专业"}, index = 7)
    @Schema(description = "是否主专业", example = "")
    private String isMajor;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否全日制"}, index = 8)
    @Schema(description = "是否全日制", example = "")
    private String isFullTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学历"}, index = 9)
    @Schema(description = "学历", example = "")
    private String education;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学位"}, index = 10)
    @Schema(description = "学位", example = "")
    private String degree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学位"}, index = 11)
    @Schema(description = "是否最高学位", example = "")
    private String isHighestDegree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学历"}, index = 12)
    @Schema(description = "是否最高学历", example = "")
    private String isHighestEducation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学业完成情况"}, index = 13)
    @Schema(description = "学业完成情况", example = "")
    private String completionStatus;
    
    public HrEmpEducation() {
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
    
	public void setSchoolEntryDate(LocalDateTime schoolEntryDate) {
		this.schoolEntryDate = schoolEntryDate;
	}
	
	public LocalDateTime getSchoolEntryDate() {
		return this.schoolEntryDate;
	}
    
	public void setSchoolEntryDateStartTime(LocalDateTime schoolEntryDateStartTime) {
		this.schoolEntryDateStartTime = schoolEntryDateStartTime;
	}
	
	public LocalDateTime getSchoolEntryDateStartTime() {
		return this.schoolEntryDateStartTime;
	}
    
    public void setSchoolEntryDateEndTime(LocalDateTime schoolEntryDateEndTime) {
		this.schoolEntryDateEndTime = schoolEntryDateEndTime;
	}
	
	public LocalDateTime getSchoolEntryDateEndTime() {
		return this.schoolEntryDateEndTime;
	}

	public void setGraduationDate(LocalDateTime graduationDate) {
		this.graduationDate = graduationDate;
	}
	
	public LocalDateTime getGraduationDate() {
		return this.graduationDate;
	}
    
	public void setGraduationDateStartTime(LocalDateTime graduationDateStartTime) {
		this.graduationDateStartTime = graduationDateStartTime;
	}
	
	public LocalDateTime getGraduationDateStartTime() {
		return this.graduationDateStartTime;
	}
    
    public void setGraduationDateEndTime(LocalDateTime graduationDateEndTime) {
		this.graduationDateEndTime = graduationDateEndTime;
	}
	
	public LocalDateTime getGraduationDateEndTime() {
		return this.graduationDateEndTime;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}
	
	public String getSchoolName() {
		return this.schoolName;
	}
    
	public void setMajor(String major) {
		this.major = major;
	}
	
	public String getMajor() {
		return this.major;
	}
    
	public void setCollege(String college) {
		this.college = college;
	}
	
	public String getCollege() {
		return this.college;
	}
    
	public void setIsMajor(String isMajor) {
		this.isMajor = isMajor;
	}
	
	public String getIsMajor() {
		return this.isMajor;
	}
    
	public void setIsFullTime(String isFullTime) {
		this.isFullTime = isFullTime;
	}
	
	public String getIsFullTime() {
		return this.isFullTime;
	}
    
	public void setEducation(String education) {
		this.education = education;
	}
	
	public String getEducation() {
		return this.education;
	}
    
	public void setDegree(String degree) {
		this.degree = degree;
	}
	
	public String getDegree() {
		return this.degree;
	}
    
	public void setIsHighestDegree(String isHighestDegree) {
		this.isHighestDegree = isHighestDegree;
	}
	
	public String getIsHighestDegree() {
		return this.isHighestDegree;
	}
    
	public void setIsHighestEducation(String isHighestEducation) {
		this.isHighestEducation = isHighestEducation;
	}
	
	public String getIsHighestEducation() {
		return this.isHighestEducation;
	}
    
	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}
	
	public String getCompletionStatus() {
		return this.completionStatus;
	}
    

    @Override
    public String toString() {
        return "HrEmpEducation{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",schoolEntryDate='" + schoolEntryDate + "'" + 
		",graduationDate='" + graduationDate + "'" + 
		",schoolName='" + schoolName + "'" + 
		",major='" + major + "'" + 
		",college='" + college + "'" + 
		",isMajor='" + isMajor + "'" + 
		",isFullTime='" + isFullTime + "'" + 
		",education='" + education + "'" + 
		",degree='" + degree + "'" + 
		",isHighestDegree='" + isHighestDegree + "'" + 
		",isHighestEducation='" + isHighestEducation + "'" + 
		",completionStatus='" + completionStatus + "'" + 
        '}';
    }
}
