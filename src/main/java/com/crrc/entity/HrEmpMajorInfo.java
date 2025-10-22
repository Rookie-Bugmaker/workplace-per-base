package com.crrc.entity;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("data_major_info")
@Schema(description = "大学专业信息表(字典)")
@Data
public class HrEmpMajorInfo extends BaseEntity implements Serializable {

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"主键"}, index = 0)
    @Schema(description = "主键")
    private String id;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业名称"}, index = 1)
    @Schema(description = "专业名称")
    private String major;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业编码"}, index = 2)
    @Schema(description = "专业编码")
    private String majorCode;
}
