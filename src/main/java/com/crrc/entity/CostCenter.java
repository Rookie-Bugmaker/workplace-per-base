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

/**
 * @author yukun
 */
@Data
@NoArgsConstructor
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_cost_center")
@Schema(description = "成本中心表")
public class CostCenter extends BaseEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 其实是公司名(编码)
     */
    @QueryMethod(method = QueryType.EQ)
    private String collection;

    @ExcelProperty(value = {"表集合"}, index = 0)
    @Schema(description = "表集合")
    @TableField(exist = false)
    private String collectionName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"成本中心编码"}, index = 1)
    @Schema(description = "成本中心编码")
    private String code;

    @QueryMethod(method = QueryType.LIKE)
    @ExcelProperty(value = {"成本中心名称"}, index = 2)
    @Schema(description = "成本中心名称")
    private String name;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"状态"}, index = 3)
    @Schema(description = "状态")
    private String status;
}
