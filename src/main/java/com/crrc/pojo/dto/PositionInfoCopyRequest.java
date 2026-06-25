package com.crrc.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 职位复制请求。
 */
@Data
@Schema(description = "职位复制请求")
public class PositionInfoCopyRequest {
    @Schema(description = "源职位ID列表")
    private List<Long> idList;

    @Schema(description = "源职位编码列表，idList为空时使用")
    private List<String> codeList;

    @Schema(description = "目标组织单元编码")
    private String targetOrgUnit;

    @Schema(description = "目标部门编码")
    private String targetDeptCode;

    @Schema(description = "目标部门名称")
    private String targetDeptName;

    @Schema(description = "新职位编码映射，key=源职位编码，value=新职位编码")
    private Map<String, String> newCodeMap;

    @Schema(description = "是否保留上下级职位关系，默认false")
    private Boolean keepParentRelation;
}
