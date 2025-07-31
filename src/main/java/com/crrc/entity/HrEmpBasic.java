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
 * 员工基本信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_basic")
@Schema(description = "员工基本信息表")
public class HrEmpBasic extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021422334177280L;

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
    @ExcelProperty(value = {"性别"}, index = 2)
    @Schema(description = "性别", example = "")
    private String gender;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"民族"}, index = 3)
    @Schema(description = "民族", example = "")
    private String nation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生地"}, index = 4)
    @Schema(description = "出生地", example = "")
    private String nativePlace;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国籍"}, index = 5)
    @Schema(description = "国籍", example = "")
    private String nationality;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "birthDateStartTime", end = "birthDateEndTime")
    @ExcelProperty(value = {"出生日期"}, index = 6)
    @Schema(description = "出生日期", example = "")
    private LocalDateTime birthDate;
    
  	@TableField(exist = false)
    @Schema(description = "出生日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime birthDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "出生日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime birthDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"年龄"}, index = 7)
    @Schema(description = "年龄", example = "")
    private Integer age;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生地"}, index = 8)
    @Schema(description = "出生地", example = "")
    private String birthPlace;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"政治面貌"}, index = 9)
    @Schema(description = "政治面貌", example = "")
    private String political;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"}, index = 10)
    @Schema(description = "姓名", example = "")
    private String name;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "partyJoinDateStartTime", end = "partyJoinDateEndTime")
    @ExcelProperty(value = {"加入党组织时间"}, index = 11)
    @Schema(description = "加入党组织时间", example = "")
    private LocalDateTime partyJoinDate;
    
  	@TableField(exist = false)
    @Schema(description = "加入党组织时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime partyJoinDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "加入党组织时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime partyJoinDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "workDateStartTime", end = "workDateEndTime")
    @ExcelProperty(value = {"开始工作日期"}, index = 12)
    @Schema(description = "开始工作日期", example = "")
    private LocalDateTime workDate;
    
  	@TableField(exist = false)
    @Schema(description = "开始工作日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime workDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "开始工作日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime workDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "entryDateStartTime", end = "entryDateEndTime")
    @ExcelProperty(value = {"入司日期"}, index = 13)
    @Schema(description = "入司日期", example = "")
    private LocalDateTime entryDate;
    
  	@TableField(exist = false)
    @Schema(description = "入司日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime entryDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "入司日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime entryDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工来源"}, index = 14)
    @Schema(description = "员工来源", example = "")
    private String empSource;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"婚姻状况"}, index = 15)
    @Schema(description = "婚姻状况", example = "")
    private String maritalStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"身份类型"}, index = 16)
    @Schema(description = "身份类型", example = "")
    private String identityType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"}, index = 17)
    @Schema(description = "职务层级", example = "")
    private String empJob;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"}, index = 18)
    @Schema(description = "职级层级", example = "")
    private String empRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"校招届数"}, index = 19)
    @Schema(description = "校招届数", example = "")
    private String campusPeriod;
    
    public HrEmpBasic() {
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
    
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getGender() {
		return this.gender;
	}
    
	public void setNation(String nation) {
		this.nation = nation;
	}
	
	public String getNation() {
		return this.nation;
	}
    
	public void setNativePlace(String nativePlace) {
		this.nativePlace = nativePlace;
	}
	
	public String getNativePlace() {
		return this.nativePlace;
	}
    
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
	
	public String getNationality() {
		return this.nationality;
	}
    
	public void setBirthDate(LocalDateTime birthDate) {
		this.birthDate = birthDate;
	}
	
	public LocalDateTime getBirthDate() {
		return this.birthDate;
	}
    
	public void setBirthDateStartTime(LocalDateTime birthDateStartTime) {
		this.birthDateStartTime = birthDateStartTime;
	}
	
	public LocalDateTime getBirthDateStartTime() {
		return this.birthDateStartTime;
	}
    
    public void setBirthDateEndTime(LocalDateTime birthDateEndTime) {
		this.birthDateEndTime = birthDateEndTime;
	}
	
	public LocalDateTime getBirthDateEndTime() {
		return this.birthDateEndTime;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
	public Integer getAge() {
		return this.age;
	}
    
	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}
	
	public String getBirthPlace() {
		return this.birthPlace;
	}
    
	public void setPolitical(String political) {
		this.political = political;
	}
	
	public String getPolitical() {
		return this.political;
	}
    
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
    
	public void setPartyJoinDate(LocalDateTime partyJoinDate) {
		this.partyJoinDate = partyJoinDate;
	}
	
	public LocalDateTime getPartyJoinDate() {
		return this.partyJoinDate;
	}
    
	public void setPartyJoinDateStartTime(LocalDateTime partyJoinDateStartTime) {
		this.partyJoinDateStartTime = partyJoinDateStartTime;
	}
	
	public LocalDateTime getPartyJoinDateStartTime() {
		return this.partyJoinDateStartTime;
	}
    
    public void setPartyJoinDateEndTime(LocalDateTime partyJoinDateEndTime) {
		this.partyJoinDateEndTime = partyJoinDateEndTime;
	}
	
	public LocalDateTime getPartyJoinDateEndTime() {
		return this.partyJoinDateEndTime;
	}

	public void setWorkDate(LocalDateTime workDate) {
		this.workDate = workDate;
	}
	
	public LocalDateTime getWorkDate() {
		return this.workDate;
	}
    
	public void setWorkDateStartTime(LocalDateTime workDateStartTime) {
		this.workDateStartTime = workDateStartTime;
	}
	
	public LocalDateTime getWorkDateStartTime() {
		return this.workDateStartTime;
	}
    
    public void setWorkDateEndTime(LocalDateTime workDateEndTime) {
		this.workDateEndTime = workDateEndTime;
	}
	
	public LocalDateTime getWorkDateEndTime() {
		return this.workDateEndTime;
	}

	public void setEntryDate(LocalDateTime entryDate) {
		this.entryDate = entryDate;
	}
	
	public LocalDateTime getEntryDate() {
		return this.entryDate;
	}
    
	public void setEntryDateStartTime(LocalDateTime entryDateStartTime) {
		this.entryDateStartTime = entryDateStartTime;
	}
	
	public LocalDateTime getEntryDateStartTime() {
		return this.entryDateStartTime;
	}
    
    public void setEntryDateEndTime(LocalDateTime entryDateEndTime) {
		this.entryDateEndTime = entryDateEndTime;
	}
	
	public LocalDateTime getEntryDateEndTime() {
		return this.entryDateEndTime;
	}

	public void setEmpSource(String empSource) {
		this.empSource = empSource;
	}
	
	public String getEmpSource() {
		return this.empSource;
	}
    
	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	
	public String getMaritalStatus() {
		return this.maritalStatus;
	}
    
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}
	
	public String getIdentityType() {
		return this.identityType;
	}
    
	public void setEmpJob(String empJob) {
		this.empJob = empJob;
	}
	
	public String getEmpJob() {
		return this.empJob;
	}
    
	public void setEmpRank(String empRank) {
		this.empRank = empRank;
	}
	
	public String getEmpRank() {
		return this.empRank;
	}
    
	public void setCampusPeriod(String campusPeriod) {
		this.campusPeriod = campusPeriod;
	}
	
	public String getCampusPeriod() {
		return this.campusPeriod;
	}
    

    @Override
    public String toString() {
        return "HrEmpBasic{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",gender='" + gender + "'" + 
		",nation='" + nation + "'" + 
		",nativePlace='" + nativePlace + "'" + 
		",nationality='" + nationality + "'" + 
		",birthDate='" + birthDate + "'" + 
		",age='" + age + "'" + 
		",birthPlace='" + birthPlace + "'" + 
		",political='" + political + "'" + 
		",name='" + name + "'" + 
		",partyJoinDate='" + partyJoinDate + "'" + 
		",workDate='" + workDate + "'" + 
		",entryDate='" + entryDate + "'" + 
		",empSource='" + empSource + "'" + 
		",maritalStatus='" + maritalStatus + "'" + 
		",identityType='" + identityType + "'" + 
		",empJob='" + empJob + "'" + 
		",empRank='" + empRank + "'" + 
		",campusPeriod='" + campusPeriod + "'" + 
        '}';
    }
}
