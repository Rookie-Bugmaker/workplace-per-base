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
 * 员工履历信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_resume")
@Schema(description = "员工履历信息表")
@Data
@NoArgsConstructor
public class HrEmpResume extends BaseEntity implements Serializable {

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
    @ExcelProperty(value = {"开始日期"}, index = 2)
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结束日期"}, index = 3)
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作单位"}, index = 4)
    @Schema(description = "工作单位")
    private String unit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作部门"}, index = 5)
    @Schema(description = "工作部门")
    private String department;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"}, index = 6)
    @Schema(description = "职位")
    private String position;
}
