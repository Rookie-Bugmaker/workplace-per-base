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
 * 职业发展通道
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_career_path")
@Schema(description = "职业发展通道")
public class HrEmpCareerPath extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021418202787840L;

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
    @ExcelProperty(value = {"职业发展通道"}, index = 2)
    @Schema(description = "职业发展通道", example = "")
    private String pathId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展等级"}, index = 3)
    @Schema(description = "职业发展等级", example = "")
    private String levelId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展等级名称"}, index = 4)
    @Schema(description = "职业发展等级名称", example = "")
    private String levelName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展层级"}, index = 5)
    @Schema(description = "职业发展层级", example = "")
    private String hierarchy;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "qualificationDateStartTime", end = "qualificationDateEndTime")
    @ExcelProperty(value = {"获得资格时间"}, index = 6)
    @Schema(description = "获得资格时间", example = "")
    private LocalDateTime qualificationDate;
    
  	@TableField(exist = false)
    @Schema(description = "获得资格时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime qualificationDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "获得资格时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime qualificationDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "effectiveDateStartTime", end = "effectiveDateEndTime")
    @ExcelProperty(value = {"既有等级生效时间"}, index = 7)
    @Schema(description = "既有等级生效时间", example = "")
    private LocalDateTime effectiveDate;
    
  	@TableField(exist = false)
    @Schema(description = "既有等级生效时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime effectiveDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "既有等级生效时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime effectiveDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "expiryDateStartTime", end = "expiryDateEndTime")
    @ExcelProperty(value = {"失效时间"}, index = 8)
    @Schema(description = "失效时间", example = "")
    private LocalDateTime expiryDate;
    
  	@TableField(exist = false)
    @Schema(description = "失效时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime expiryDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "失效时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime expiryDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否失效"}, index = 9)
    @Schema(description = "是否失效", example = "")
    private String isExpired;
    
    public HrEmpCareerPath() {
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
    
	public void setPathId(String pathId) {
		this.pathId = pathId;
	}
	
	public String getPathId() {
		return this.pathId;
	}
    
	public void setLevelId(String levelId) {
		this.levelId = levelId;
	}
	
	public String getLevelId() {
		return this.levelId;
	}
    
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	
	public String getLevelName() {
		return this.levelName;
	}
    
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	public String getHierarchy() {
		return this.hierarchy;
	}
    
	public void setQualificationDate(LocalDateTime qualificationDate) {
		this.qualificationDate = qualificationDate;
	}
	
	public LocalDateTime getQualificationDate() {
		return this.qualificationDate;
	}
    
	public void setQualificationDateStartTime(LocalDateTime qualificationDateStartTime) {
		this.qualificationDateStartTime = qualificationDateStartTime;
	}
	
	public LocalDateTime getQualificationDateStartTime() {
		return this.qualificationDateStartTime;
	}
    
    public void setQualificationDateEndTime(LocalDateTime qualificationDateEndTime) {
		this.qualificationDateEndTime = qualificationDateEndTime;
	}
	
	public LocalDateTime getQualificationDateEndTime() {
		return this.qualificationDateEndTime;
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

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public LocalDateTime getExpiryDate() {
		return this.expiryDate;
	}
    
	public void setExpiryDateStartTime(LocalDateTime expiryDateStartTime) {
		this.expiryDateStartTime = expiryDateStartTime;
	}
	
	public LocalDateTime getExpiryDateStartTime() {
		return this.expiryDateStartTime;
	}
    
    public void setExpiryDateEndTime(LocalDateTime expiryDateEndTime) {
		this.expiryDateEndTime = expiryDateEndTime;
	}
	
	public LocalDateTime getExpiryDateEndTime() {
		return this.expiryDateEndTime;
	}

	public void setIsExpired(String isExpired) {
		this.isExpired = isExpired;
	}
	
	public String getIsExpired() {
		return this.isExpired;
	}
    

    @Override
    public String toString() {
        return "HrEmpCareerPath{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",pathId='" + pathId + "'" + 
		",levelId='" + levelId + "'" + 
		",levelName='" + levelName + "'" + 
		",hierarchy='" + hierarchy + "'" + 
		",qualificationDate='" + qualificationDate + "'" + 
		",effectiveDate='" + effectiveDate + "'" + 
		",expiryDate='" + expiryDate + "'" + 
		",isExpired='" + isExpired + "'" + 
        '}';
    }
}
