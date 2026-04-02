package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=false)
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterListVO {

    @Schema(description = "档案号")
    private String userCode;

    @Schema(description = "姓名")
    private String userName;

    @Schema(description = "员工所属部门")
    private String dept;

    @Schema(description = "成本中心编码")
    private String costcenterCode;

    @Schema(description = "成本中心名称")
    private String costcenterName;
}
