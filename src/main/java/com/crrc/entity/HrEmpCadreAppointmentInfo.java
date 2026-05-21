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
import java.time.LocalDateTime;

/**
 * <p>
 * 中干任职信息表
 * </p>
 *
 * @author yukun
 * @since 2026-05-19
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_cadre_appointment_info")
@Schema(description = "中干任职信息表")
@Data
@NoArgsConstructor
public class HrEmpCadreAppointmentInfo extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案号"})
    @Schema(description = "档案号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织"})
    @Schema(description = "组织")
    private String org;

    @TableField(exist = false)
    private String orgValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"})
    @Schema(description = "部门")
    private String dept;

    @TableField(exist = false)
    private String deptValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织单元"})
    @Schema(description = "组织单元")
    private String orgUnit;

    @TableField(exist = false)
    private String orgUnitValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"})
    @Schema(description = "职位")
    private String position;

    @TableField(exist = false)
    private String positionValue;

    @ExcelProperty(value = {"任期开始时间"})
    @Schema(description = "任期开始时间")
    private LocalDateTime tenureStartTime;

    @ExcelProperty(value = {"任期结束时间"})
    @Schema(description = "任期结束时间")
    private LocalDateTime tenureEndTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"试用期(0)/任期(1)"})
    @Schema(description = "试用期(0)/任期(1)")
    private String probationStatus;

    @TableField(exist = false)
    private String probationStatusValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职种"})
    @Schema(description = "职种")
    private String jobType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"排序号"})
    @Schema(description = "排序号")
    private Integer sortNo;
}
