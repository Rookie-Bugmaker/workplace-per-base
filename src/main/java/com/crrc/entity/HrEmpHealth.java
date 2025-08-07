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
 * 员工健康信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_health")
@Schema(description = "员工健康信息表")
public class HrEmpHealth extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021422954934272L;

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
    @ExcelProperty(value = {"健康状况"}, index = 2)
    @Schema(description = "健康状况", example = "")
    private String healthStatus;
    
    public HrEmpHealth() {
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
    
	public void setHealthStatus(String healthStatus) {
		this.healthStatus = healthStatus;
	}
	
	public String getHealthStatus() {
		return this.healthStatus;
	}
    

    @Override
    public String toString() {
        return "HrEmpHealth{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",healthStatus='" + healthStatus + "'" + 
        '}';
    }
}
