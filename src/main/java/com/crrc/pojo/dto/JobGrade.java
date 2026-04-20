package com.crrc.pojo.dto;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.entity.BaseEntity;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 岗级记录表
 * </p>
 *
 * @author yukun
 * @since 2025-12-25 17:02:11
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@Schema(description = "岗级")
public class JobGrade extends BaseEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗级编码"})
    @Schema(description = "岗级编码")
    private String code;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗级"})
    @Schema(description = "岗级")
    private String salaryGrade;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"岗位工资"})
    @Schema(description = "岗位工资")
    private BigDecimal positionSalary;

}