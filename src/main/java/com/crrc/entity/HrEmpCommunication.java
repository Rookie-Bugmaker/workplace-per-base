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
@Data
@NoArgsConstructor
public class HrEmpCommunication extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"移动电话"}, index = 2)
    @Schema(description = "移动电话")
    private String mobilePhone;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作邮箱"}, index = 3)
    @Schema(description = "工作邮箱")
    private String workEmail;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人邮箱"}, index = 4)
    @Schema(description = "个人邮箱")
    private String personalEmail;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"家庭住址"}, index = 5)
    @Schema(description = "家庭住址")
    private String address;
}
