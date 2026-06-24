package com.crrc.pojo.vo;

import com.crrc.entity.PositionDescription;
import com.crrc.entity.PositionDescriptionAssessment;
import com.crrc.entity.PositionDescriptionAttachment;
import com.crrc.entity.PositionDescriptionAuditRecord;
import com.crrc.entity.PositionDescriptionAuthority;
import com.crrc.entity.PositionDescriptionBasis;
import com.crrc.entity.PositionDescriptionCompetency;
import com.crrc.entity.PositionDescriptionCoordination;
import com.crrc.entity.PositionDescriptionResponsibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位说明书详情聚合视图。
 */
@Data
@Schema(description = "职位说明书详情聚合视图")
public class PositionDescriptionDetailVO {
    @Schema(description = "职位说明书主表")
    private PositionDescription description;

    @Schema(description = "职位职责")
    private List<PositionDescriptionResponsibility> responsibilities = new ArrayList<>();

    @Schema(description = "工作依据")
    private List<PositionDescriptionBasis> basisList = new ArrayList<>();

    @Schema(description = "协调内容")
    private List<PositionDescriptionCoordination> coordinationList = new ArrayList<>();

    @Schema(description = "工作权限")
    private List<PositionDescriptionAuthority> authorityList = new ArrayList<>();

    @Schema(description = "主要考核方向")
    private List<PositionDescriptionAssessment> assessmentList = new ArrayList<>();

    @Schema(description = "能力要求")
    private List<PositionDescriptionCompetency> competencyList = new ArrayList<>();

    @Schema(description = "附件，仅保存 MinIO fileId")
    private List<PositionDescriptionAttachment> attachments = new ArrayList<>();

    @Schema(description = "审批记录")
    private List<PositionDescriptionAuditRecord> auditRecords = new ArrayList<>();
}
