package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任职信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_appointment")
@Schema(description = "任职信息")
@Data
@NoArgsConstructor
public class HrEmpAppointment extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"开始日期"}, index = 0)
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结束日期"}, index = 1)
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动事件"}, index = 2)
    @Schema(description = "变动事件")
    private String changeEvent;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动类型"}, index = 3)
    @Schema(description = "变动类型")
    private String changeType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"变动原因"}, index = 4)
    @Schema(description = "变动原因")
    private String changeReason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"}, index = 5)
    @Schema(description = "人员类别")
    private String personnelCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"在岗状态"}, index = 6)
    @Schema(description = "在岗状态")
    private String dutyStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织"}, index = 7)
    @Schema(description = "组织")
    private String org;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"}, index = 8)
    @Schema(description = "部门")
    private String dept;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织单元"}, index = 9)
    @Schema(description = "组织单元")
    private String orgUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"}, index = 10)
    @Schema(description = "职位")
    private String position;

    @TableField(exist = false)
    private String positionValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"}, index = 11)
    @Schema(description = "职级层级")
    private String hierarchyRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务名称"}, index = 12)
    @Schema(description = "职务名称")
    private String positionName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"}, index = 13)
    @Schema(description = "职务层级")
    private String positionHierarchy;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务类别"}, index = 14)
    @Schema(description = "职务类别")
    private String positionCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"薪资组"}, index = 15)
    @Schema(description = "薪资组")
    private String salaryGroup;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 17)
    @Schema(description = "工号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职种编码"}, index = 18)
    @Schema(description = "职种编码")
    private String jobType;
}
