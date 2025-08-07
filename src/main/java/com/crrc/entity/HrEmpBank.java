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
 * 银行账户
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_bank")
@Schema(description = "银行账户")
public class HrEmpBank extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021420425768960L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;


    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"银行"}, index = 0)
    @Schema(description = "银行", example = "")
    private String bankName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"账号"}, index = 1)
    @Schema(description = "账号", example = "")
    private String bankAccount;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"开户行"}, index = 2)
    @Schema(description = "开户行", example = "")
    private String openingBank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"账户类型"}, index = 3)
    @Schema(description = "账户类型", example = "")
    private String accountType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"币种"}, index = 4)
    @Schema(description = "币种", example = "")
    private String currency;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户名"}, index = 5)
    @Schema(description = "户名", example = "")
    private String accountName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否首选"}, index = 6)
    @Schema(description = "是否首选", example = "")
    private String isPrimary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 8)
    @Schema(description = "工号", example = "")
    private String userCode;
    
    public HrEmpBank() {
    }

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	public String getBankName() {
		return this.bankName;
	}
    
	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}
	
	public String getBankAccount() {
		return this.bankAccount;
	}
    
	public void setOpeningBank(String openingBank) {
		this.openingBank = openingBank;
	}
	
	public String getOpeningBank() {
		return this.openingBank;
	}
    
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
	public String getAccountType() {
		return this.accountType;
	}
    
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public String getCurrency() {
		return this.currency;
	}
    
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getAccountName() {
		return this.accountName;
	}
    
	public void setIsPrimary(String isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	public String getIsPrimary() {
		return this.isPrimary;
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
    

    @Override
    public String toString() {
        return "HrEmpBank{" +
		"bankName=" + bankName +
		",bankAccount='" + bankAccount + "'" + 
		",openingBank='" + openingBank + "'" + 
		",accountType='" + accountType + "'" + 
		",currency='" + currency + "'" + 
		",accountName='" + accountName + "'" + 
		",isPrimary='" + isPrimary + "'" + 
		",id='" + id + "'" + 
		",userCode='" + userCode + "'" + 
        '}';
    }
}
