package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import com.crrc.utils.DiffMarkable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 员工主要家庭成员社会关系表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_fam_member")
@Schema(description = "员工主要家庭成员社会关系表")
@Data
@NoArgsConstructor
public class HrEmpFamMember extends BaseEntity implements Serializable, DiffMarkable {

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
    @ExcelProperty(value = {"姓名"}, index = 2)
    @Schema(description = "姓名")
    private String famName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"称谓"}, index = 3)
    @Schema(description = "称谓")
    private String relation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生日期"}, index = 4)
    @Schema(description = "出生日期")
    private LocalDate famBirthDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否紧急联系人"}, index = 5)
    @Schema(description = "是否紧急联系人")
    private String isEmergencyContact;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作单位"}, index = 6)
    @Schema(description = "工作单位")
    private String famUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务"}, index = 7)
    @Schema(description = "职务")
    private String famPosition;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"政治面貌"}, index = 8)
    @Schema(description = "政治面貌")
    private String famPoliticalStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"联系电话"}, index = 9)
    @Schema(description = "联系电话")
    private String famPhone;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"}, index = 10)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @Schema(description = "变动字段统计")
    @TableField(exist = false)
    private List<String> markList;
}
