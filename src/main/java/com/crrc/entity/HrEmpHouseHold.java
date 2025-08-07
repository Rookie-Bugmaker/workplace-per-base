package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
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
@TableName("hr_emp_house_hold")
@Schema(description = "员工户口本信息表")
@Data
public class HrEmpHouseHold extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419486244864L;

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
    @ExcelProperty(value = {"户口本类型"}, index = 2)
    @Schema(description = "户口本类型")
    private String houseHoldType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户口本所在地"}, index = 3)
    @Schema(description = "户口本所在地")
    private String houseHoldLocation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"户口本详细地址"}, index = 4)
    @Schema(description = "户口本详细地址")
    private String houseHoldLocationDetail;
    
    public HrEmpHouseHold() {
    }

}
