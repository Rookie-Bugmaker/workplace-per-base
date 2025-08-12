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
@TableName("data_school_info")
@Schema(description = "毕业院校信息表")
@Data
public class HrEmpSchoolInfo extends BaseEntity implements Serializable {

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"主键"}, index = 0)
    @Schema(description = "主键")
    private String id;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"校名"}, index = 1)
    @Schema(description = "校名")
    private String schoolName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校编码"}, index = 2)
    @Schema(description = "学校编码")
    private String schoolCode;
}
