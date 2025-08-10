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
 * 员工职业技能等级表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_professional_skill")
@Schema(description = "员工职业技能等级表")
@Data
@NoArgsConstructor
public class HrEmpProfessionalSkill extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021423265312768L;

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
    @ExcelProperty(value = {"专业技术名称"}, index = 2)
    @Schema(description = "专业技术名称")
    private String skillName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业技术等级"}, index = 3)
    @Schema(description = "专业技术等级")
    private String skillLevel;
}
