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
 * 员工证件信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_certificate")
@Schema(description = "员工证件信息表")
public class HrEmpCertificate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021421688254464L;

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
    @ExcelProperty(value = {"证件类型"}, index = 2)
    @Schema(description = "证件类型", example = "")
    private String certiType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证件号码"}, index = 3)
    @Schema(description = "证件号码", example = "")
    private String certiId;
    
    public HrEmpCertificate() {
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
    
	public void setCertiType(String certiType) {
		this.certiType = certiType;
	}
	
	public String getCertiType() {
		return this.certiType;
	}
    
	public void setCertiId(String certiId) {
		this.certiId = certiId;
	}
	
	public String getCertiId() {
		return this.certiId;
	}
    

    @Override
    public String toString() {
        return "HrEmpCertificate{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",certiType='" + certiType + "'" + 
		",certiId='" + certiId + "'" + 
        '}';
    }
}
