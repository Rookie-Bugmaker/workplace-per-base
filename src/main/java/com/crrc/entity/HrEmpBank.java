package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@NoArgsConstructor
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
    @Schema(description = "银行")
    private String bankName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"账号"}, index = 1)
    @Schema(description = "账号")
    private String bankAccount;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"开户行"}, index = 2)
    @Schema(description = "开户行")
    private String openingBank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"账户类型"}, index = 3)
    @Schema(description = "账户类型")
    private String accountType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"币种"}, index = 4)
    @Schema(description = "币种")
    private String currency;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户名"}, index = 5)
    @Schema(description = "户名")
    private String accountName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否首选"}, index = 6)
    @Schema(description = "是否首选")
    private String isPrimary;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 8)
    @Schema(description = "工号")
    private String userCode;
}
