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
 * 员工教育信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_education")
@Schema(description = "员工教育信息表")
@Data
@NoArgsConstructor
public class HrEmpEducation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021423575691264L;

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

    @ExcelProperty(value = {"入学时间"}, index = 2)
    @Schema(description = "入学时间")
    private LocalDate schoolEntryDate;

    @ExcelProperty(value = {"毕业时间"}, index = 3)
    @Schema(description = "毕业时间")
    private LocalDate graduationDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校名称"}, index = 4)
    @Schema(description = "学校名称")
    @TableField(exist = false)
    private String schoolName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业名称"}, index = 5)
    @Schema(description = "专业名称")
    @TableField(exist = false)
    private String major;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学院"}, index = 6)
    @Schema(description = "学院")
    private String college;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否主专业"}, index = 7)
    @Schema(description = "是否主专业")
    private String isMajor;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否全日制"}, index = 8)
    @Schema(description = "是否全日制")
    private String isFullTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学历"}, index = 9)
    @Schema(description = "学历")
    private String education;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学位"}, index = 10)
    @Schema(description = "学位")
    private String degree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学位"}, index = 11)
    @Schema(description = "是否最高学位")
    private String isHighestDegree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学历"}, index = 12)
    @Schema(description = "是否最高学历")
    private String isHighestEducation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学业完成情况"}, index = 13)
    @Schema(description = "学业完成情况")
    private String completionStatus;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学位专业"}, index = 14)
    @Schema(description = "学位专业")
    private String degreeMajor;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校编码"}, index = 15)
    @Schema(description = "学校编码")
    private String schoolCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业编码"}, index = 16)
    @Schema(description = "专业编码")
    private String majorCode;
}
