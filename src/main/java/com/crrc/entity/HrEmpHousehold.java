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
 * 员工户口本信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_household")
@Schema(description = "员工户口本信息表")
public class HrEmpHousehold extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419486244864L;

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
    @ExcelProperty(value = {"户口本类型"}, index = 2)
    @Schema(description = "户口本类型", example = "")
    private String householeType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户口本所在地"}, index = 3)
    @Schema(description = "户口本所在地", example = "")
    private String housesholdLocation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户口本详细地址"}, index = 4)
    @Schema(description = "户口本详细地址", example = "")
    private String householdLocationDetail;
    
    public HrEmpHousehold() {
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
    
	public void setHouseholeType(String householeType) {
		this.householeType = householeType;
	}
	
	public String getHouseholeType() {
		return this.householeType;
	}
    
	public void setHousesholdLocation(String housesholdLocation) {
		this.housesholdLocation = housesholdLocation;
	}
	
	public String getHousesholdLocation() {
		return this.housesholdLocation;
	}
    
	public void setHouseholdLocationDetail(String householdLocationDetail) {
		this.householdLocationDetail = householdLocationDetail;
	}
	
	public String getHouseholdLocationDetail() {
		return this.householdLocationDetail;
	}
    

    @Override
    public String toString() {
        return "HrEmpHousehold{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",householeType='" + householeType + "'" + 
		",housesholdLocation='" + housesholdLocation + "'" + 
		",householdLocationDetail='" + householdLocationDetail + "'" + 
        '}';
    }
}
