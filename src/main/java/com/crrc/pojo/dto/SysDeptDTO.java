package com.crrc.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysDeptDTO {
    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "父组织编码")
    private String parentOrgCode;

    @Schema(description = "部门排序")
    private Integer departmentOrder;

    @Schema(description = "组织编码")
    private String orgCode;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "关联公司信息")
    private Object company;

    @Schema(description = "状态 (0-正常, 1-停用)")
    private String status;
}
