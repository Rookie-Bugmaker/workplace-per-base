package com.crrc.pojo.dto;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

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
    @Schema(description = "部门集合")
    private Set<String> deptSet;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职位")
    private String position;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "是否离职标记，0为离职，1为在职")
    private String isEmployed;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "在岗状态")
    private String dutyStatus;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "人员分类1")
    private String category1;
}
