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
 * 员工通讯信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_communication")
@Schema(description = "员工通讯信息表")
public class HrEmpCommunication extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021422644555776L;

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
    @ExcelProperty(value = {"移动电话"}, index = 2)
    @Schema(description = "移动电话", example = "")
    private String mobilePhone;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作邮箱"}, index = 3)
    @Schema(description = "工作邮箱", example = "")
    private String workEmail;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人邮箱"}, index = 4)
    @Schema(description = "个人邮箱", example = "")
    private String pensonalEmail;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"家庭住址"}, index = 5)
    @Schema(description = "家庭住址", example = "")
    private String address;
    
    public HrEmpCommunication() {
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
    
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	
	public String getMobilePhone() {
		return this.mobilePhone;
	}
    
	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}
	
	public String getWorkEmail() {
		return this.workEmail;
	}
    
	public void setPensonalEmail(String pensonalEmail) {
		this.pensonalEmail = pensonalEmail;
	}
	
	public String getPensonalEmail() {
		return this.pensonalEmail;
	}
    
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return this.address;
	}
    

    @Override
    public String toString() {
        return "HrEmpCommunication{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",mobilePhone='" + mobilePhone + "'" + 
		",workEmail='" + workEmail + "'" + 
		",pensonalEmail='" + pensonalEmail + "'" + 
		",address='" + address + "'" + 
        '}';
    }
}
