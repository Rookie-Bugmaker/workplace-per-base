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
 * 员工专业职务任职资格表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_pro_qualification")
@Schema(description = "员工专业职务任职资格表")
@Data
@NoArgsConstructor
public class HrEmpProfessionalQualification extends BaseEntity implements Serializable {

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
    @ExcelProperty(value = {"专业技术资格"})
    @Schema(description = "专业技术资格")
    private String quaId;

    @TableField(exist = false)
    private String quaIdValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获得资格时间"})
    @Schema(description = "获得资格时间")
    private LocalDate quaDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职称级别"})
    @Schema(description = "职称级别")
    private String quaTitleLevel;

    @TableField(exist = false)
    private String quaTitleLevelValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资格审批单位"})
    @Schema(description = "资格审批单位")
    private String quaApprovalUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得资格途径"})
    @Schema(description = "取得资格途径")
    private String quaPath;

    @TableField(exist = false)
    private String quaPathValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高"})
    @Schema(description = "是否最高")
    private String isHighest;

    @TableField(exist = false)
    private String isHighestValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否聘任"})
    @Schema(description = "是否聘任")
    private String isAppointed;

    @TableField(exist = false)
    private String isAppointedValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任起始时间"})
    @Schema(description = "聘任起始时间")
    private LocalDate appointedStartDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任结束时间"})
    @Schema(description = "聘任结束时间")
    private LocalDate appointedEndDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"})
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

}
