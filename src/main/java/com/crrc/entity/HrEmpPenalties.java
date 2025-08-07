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
 * 员工惩处信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_penalties")
@Schema(description = "员工惩处信息表")
public class HrEmpPenalties extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021421369487360L;

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
    @ExcelProperty(value = {"惩处名称"}, index = 2)
    @Schema(description = "惩处名称", example = "")
    private String penaltyName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"批准单位"}, index = 3)
    @Schema(description = "批准单位", example = "")
    private String penaltyUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处文件号"}, index = 4)
    @Schema(description = "惩处文件号", example = "")
    private String penaltyDocId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处原因"}, index = 5)
    @Schema(description = "惩处原因", example = "")
    private String penaltyReason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"惩处措施"}, index = 6)
    @Schema(description = "惩处措施", example = "")
    private String penaltyMeasures;
    
    public HrEmpPenalties() {
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
    
	public void setPenaltyName(String penaltyName) {
		this.penaltyName = penaltyName;
	}
	
	public String getPenaltyName() {
		return this.penaltyName;
	}
    
	public void setPenaltyUnit(String penaltyUnit) {
		this.penaltyUnit = penaltyUnit;
	}
	
	public String getPenaltyUnit() {
		return this.penaltyUnit;
	}
    
	public void setPenaltyDocId(String penaltyDocId) {
		this.penaltyDocId = penaltyDocId;
	}
	
	public String getPenaltyDocId() {
		return this.penaltyDocId;
	}
    
	public void setPenaltyReason(String penaltyReason) {
		this.penaltyReason = penaltyReason;
	}
	
	public String getPenaltyReason() {
		return this.penaltyReason;
	}
    
	public void setPenaltyMeasures(String penaltyMeasures) {
		this.penaltyMeasures = penaltyMeasures;
	}
	
	public String getPenaltyMeasures() {
		return this.penaltyMeasures;
	}
    

    @Override
    public String toString() {
        return "HrEmpPenalties{" +
		"id=" + id +
		",userCode='" + userCode + "'" + 
		",penaltyName='" + penaltyName + "'" + 
		",penaltyUnit='" + penaltyUnit + "'" + 
		",penaltyDocId='" + penaltyDocId + "'" + 
		",penaltyReason='" + penaltyReason + "'" + 
		",penaltyMeasures='" + penaltyMeasures + "'" + 
        '}';
    }
}
