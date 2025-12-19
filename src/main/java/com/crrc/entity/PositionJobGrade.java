package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 职位-岗级/标准职级表
 * </p>
 *
 * @author yukun
 * @since 2025-12-19 08:35:16
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("position_job_grade")
@Schema(description = "职位-岗级/标准职级表")
@Data
@NoArgsConstructor
public class PositionJobGrade extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2001813546987708416L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗级/标准职级"}, index = 0)
    @Schema(description = "岗级/标准职级", example = "")
    private String jobGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗位工资"}, index = 1)
    @Schema(description = "岗位工资", example = "")
    private BigDecimal jobSalary;

    @Override
    public String toString() {
        return "PositionJobGrade{" +
		"id=" + id +
		",jobGrade='" + jobGrade + "'" + 
		",jobSalary='" + jobSalary + "'" + 
        '}';
    }
}
