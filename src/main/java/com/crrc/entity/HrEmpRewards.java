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
 * 员工奖励情况表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_rewards")
@Schema(description = "员工奖励情况表")
@Data
@NoArgsConstructor
public class HrEmpRewards extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021418517360640L;

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
    @ExcelProperty(value = {"奖励类别"}, index = 2)
    @Schema(description = "奖励类别", example = "")
    private String rewardId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励名称"}, index = 3)
    @Schema(description = "奖励名称", example = "")
    private String rewardName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授奖单位"}, index = 4)
    @Schema(description = "授奖单位", example = "")
    private String rewardUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授予荣誉称号级别"}, index = 5)
    @Schema(description = "授予荣誉称号级别", example = "")
    private String honoraryTitleLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励类型"}, index = 6)
    @Schema(description = "奖励类型", example = "")
    private String rewardType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖日期"}, index = 7)
    @Schema(description = "获奖日期", example = "")
    private LocalDate rewardDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖等级"}, index = 8)
    @Schema(description = "获奖等级", example = "")
    private String rewardLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励措施"}, index = 9)
    @Schema(description = "奖励措施", example = "")
    private String rewardMeasures;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书编号"}, index = 10)
    @Schema(description = "证书编号", example = "")
    private String certiId;

}
