package com.crrc.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工信息列表查询封装实体
 */
@Data
public class HrEmpBasicSearchDTO {

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "档案号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "姓名")
    private String name;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "部门")
    private String dept;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职位")
    private String position;
}
