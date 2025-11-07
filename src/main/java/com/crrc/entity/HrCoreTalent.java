package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@NoArgsConstructor
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_core_talent")
@Schema(description = "中车核心人才信息表")
public class HrCoreTalent extends BaseEntity {
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("员工工号")
    @Schema(description = "员工工号")
    private String jobNumber;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("员工姓名")
    @Schema(description = "员工姓名")
    @TableField(exist = false)
    private String name;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("所属序列")
    @Schema(description = "所属序列")
    private String sequenceType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("层级")
    @Schema(description = "层级")
    private String talentLevel;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("聘任开始时间")
    @Schema(description = "聘任开始时间")
    private Date appointmentStartDate;

    @TableField(exist = false)
    private String appointmentStartDateStr;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("聘任结束时间")
    @Schema(description = "聘任结束时间")
    private Date appointmentEndDate;

    @TableField(exist = false)
    private String appointmentEndDateStr;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty("聘任状态")
    @Schema(description = "聘任状态")
    private String appointmentStatus;
}
