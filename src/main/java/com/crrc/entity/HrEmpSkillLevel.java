package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 职业技能等级
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_skill_level")
@Schema(description = "职业技能等级")
@Data
@NoArgsConstructor
public class HrEmpSkillLevel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419805011968L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工种"}, index = 0)
    @Schema(description = "工种", example = "")
    private String jobId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"技能等级"}, index = 1)
    @Schema(description = "技能等级", example = "")
    private String skillLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得方式"}, index = 2)
    @Schema(description = "取得方式", example = "")
    private String getMethod;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得时间"}, index = 3)
    @Schema(description = "取得时间", example = "")
    private LocalDate getDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高"}, index = 4)
    @Schema(description = "是否最高", example = "")
    private String isHighest;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否聘任"}, index = 5)
    @Schema(description = "是否聘任", example = "")
    private String isAppointed;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任起始时间"}, index = 6)
    @Schema(description = "聘任起始时间", example = "")
    private LocalDate appointedStartDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任结束时间"}, index = 7)
    @Schema(description = "聘任结束时间", example = "")
    private LocalDate appointedEndDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"发证机构"}, index = 8)
    @Schema(description = "发证机构", example = "")
    private String grantUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书编号"}, index = 9)
    @Schema(description = "证书编号", example = "")
    private String certiId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 11)
    @Schema(description = "工号", example = "")
    private String userCode;

}
