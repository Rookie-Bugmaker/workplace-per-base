package com.crrc.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 人员信息集批量导出请求DTO
 */
@Data
@Schema(description = "人员信息集批量导出请求")
public class HrEmpBatchExportRequest {

    @Schema(description = "姓名，模糊查询")
    private String name;

    @Schema(description = "档案号/工号，模糊查询")
    private String userCode;

    @Schema(description = "部门编码，精确查询")
    private String deptCode;

    @Schema(description = "要导出的信息集类型列表，为空时默认导出全部")
    private List<String> exportTypes;
}
