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
 * 员工主要家庭成员社会关系表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_fam_member")
@Schema(description = "员工主要家庭成员社会关系表")
public class HrEmpFamMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021418840322048L;

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
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"}, index = 2)
    @Schema(description = "姓名", example = "")
    private String famName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"称谓"}, index = 3)
    @Schema(description = "称谓", example = "")
    private String relation;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "famBirthDateStartTime", end = "famBirthDateEndTime")
    @ExcelProperty(value = {"出生日期"}, index = 4)
    @Schema(description = "出生日期", example = "")
    private LocalDateTime famBirthDate;
    
  	@TableField(exist = false)
    @Schema(description = "出生日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime famBirthDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "出生日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime famBirthDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否紧急联系人"}, index = 5)
    @Schema(description = "是否紧急联系人", example = "")
    private String isEmergencyContact;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作单位"}, index = 6)
    @Schema(description = "工作单位", example = "")
    private String famUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务"}, index = 7)
    @Schema(description = "职务", example = "")
    private String famPosition;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"政治面貌"}, index = 8)
    @Schema(description = "政治面貌", example = "")
    private String famPoliticalStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"联系电话"}, index = 9)
    @Schema(description = "联系电话", example = "")
    private String famPhone;
    
    public HrEmpFamMember() {
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
    
	public void setFamName(String famName) {
		this.famName = famName;
	}
	
	public String getFamName() {
		return this.famName;
	}
    
	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	public String getRelation() {
		return this.relation;
	}
    
	public void setFamBirthDate(LocalDateTime famBirthDate) {
		this.famBirthDate = famBirthDate;
	}
	
	public LocalDateTime getFamBirthDate() {
		return this.famBirthDate;
	}
    
	public void setFamBirthDateStartTime(LocalDateTime famBirthDateStartTime) {
		this.famBirthDateStartTime = famBirthDateStartTime;
	}
	
	public LocalDateTime getFamBirthDateStartTime() {
		return this.famBirthDateStartTime;
	}
    
    public void setFamBirthDateEndTime(LocalDateTime famBirthDateEndTime) {
		this.famBirthDateEndTime = famBirthDateEndTime;
	}
	
	public LocalDateTime getFamBirthDateEndTime() {
		return this.famBirthDateEndTime;
	}

	public void setIsEmergencyContact(String isEmergencyContact) {
		this.isEmergencyContact = isEmergencyContact;
	}
	
	public String getIsEmergencyContact() {
		return this.isEmergencyContact;
	}
    
	public void setFamUnit(String famUnit) {
		this.famUnit = famUnit;
	}
	
	public String getFamUnit() {
		return this.famUnit;
	}
    
	public void setFamPosition(String famPosition) {
		this.famPosition = famPosition;
	}
	
	public String getFamPosition() {
		return this.famPosition;
	}
    
	public void setFamPoliticalStatus(String famPoliticalStatus) {
		this.famPoliticalStatus = famPoliticalStatus;
	}
	
	public String getFamPoliticalStatus() {
		return this.famPoliticalStatus;
	}
    
	public void setFamPhone(String famPhone) {
		this.famPhone = famPhone;
	}
	
	public String getFamPhone() {
		return this.famPhone;
	}
    

    @Override
    public String toString() {
        return "HrEmpFamMember{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",famName='" + famName + "'" + 
		",relation='" + relation + "'" + 
		",famBirthDate='" + famBirthDate + "'" + 
		",isEmergencyContact='" + isEmergencyContact + "'" + 
		",famUnit='" + famUnit + "'" + 
		",famPosition='" + famPosition + "'" + 
		",famPoliticalStatus='" + famPoliticalStatus + "'" + 
		",famPhone='" + famPhone + "'" + 
        '}';
    }
}
