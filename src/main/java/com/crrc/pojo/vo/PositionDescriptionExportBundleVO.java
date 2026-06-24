package com.crrc.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位说明书多 Sheet 导出数据包。
 */
@Data
public class PositionDescriptionExportBundleVO {
    private List<PositionDescriptionMainExportVO> mainRows = new ArrayList<>();
    private List<PositionDescriptionResponsibilityExportVO> responsibilityRows = new ArrayList<>();
    private List<PositionDescriptionBasisExportVO> basisRows = new ArrayList<>();
    private List<PositionDescriptionCoordinationExportVO> coordinationRows = new ArrayList<>();
    private List<PositionDescriptionAuthorityExportVO> authorityRows = new ArrayList<>();
    private List<PositionDescriptionAssessmentExportVO> assessmentRows = new ArrayList<>();
    private List<PositionDescriptionCompetencyExportVO> competencyRows = new ArrayList<>();
    private List<PositionDescriptionAttachmentExportVO> attachmentRows = new ArrayList<>();
    private List<PositionDescriptionAuditRecordExportVO> auditRecordRows = new ArrayList<>();
}
