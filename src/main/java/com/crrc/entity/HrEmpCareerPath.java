package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 职业发展通道
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_career_path")
@Schema(description = "职业发展通道")
@Data
@NoArgsConstructor
public class HrEmpCareerPath extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"})
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展通道"})
    @Schema(description = "职业发展通道")
    private String pathId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展等级"})
    @Schema(description = "职业发展等级")
    private String levelId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职业发展等级名称"})
    @Schema(description = "职业发展等级名称")
    private String levelName;
    
    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职业发展层级 20205/10/30 应人力要求字段作废")
    private String hierarchy;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获得资格时间"})
    @Schema(description = "获得资格时间")
    private LocalDate qualificationDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"既有等级生效时间"})
    @Schema(description = "既有等级生效时间")
    private LocalDate effectiveDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"失效时间"})
    @Schema(description = "失效时间")
    private LocalDate expiryDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否失效"})
    @Schema(description = "是否失效")
    private String isExpired;

    @TableField(exist = false)
    private String isExpiredValue;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;
}
