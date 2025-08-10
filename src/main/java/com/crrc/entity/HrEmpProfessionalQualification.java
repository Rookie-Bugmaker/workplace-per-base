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
 * 员工专业职务任职资格表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_professional_qualification")
@Schema(description = "员工专业职务任职资格表")
@Data
@NoArgsConstructor
public class HrEmpProfessionalQualification extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021419159089152L;

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
    @ExcelProperty(value = {"专业技术资格"}, index = 2)
    @Schema(description = "专业技术资格")
    private String quaId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获得资格时间"}, index = 3)
    @Schema(description = "获得资格时间")
    private LocalDate quaDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职称级别"}, index = 4)
    @Schema(description = "职称级别")
    private String quaTitleLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资格审批单位"}, index = 5)
    @Schema(description = "资格审批单位")
    private String quaApprovalUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得资格途径"}, index = 6)
    @Schema(description = "取得资格途径")
    private String quaPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高"}, index = 7)
    @Schema(description = "是否最高")
    private String isHighest;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否聘任"}, index = 8)
    @Schema(description = "是否聘任")
    private String isAppointed;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任起始时间"}, index = 9)
    @Schema(description = "聘任起始时间")
    private LocalDate appointedStartDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"聘任结束时间"}, index = 10)
    @Schema(description = "聘任结束时间")
    private LocalDate appointedEndDate;

}
