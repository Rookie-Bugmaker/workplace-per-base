package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.constants.PositionManagementConstants;
import com.crrc.entity.LoginUser;
import com.crrc.entity.PositionDescription;
import com.crrc.entity.PositionDescriptionAssessment;
import com.crrc.entity.PositionDescriptionAttachment;
import com.crrc.entity.PositionDescriptionAuditRecord;
import com.crrc.entity.PositionDescriptionAuthority;
import com.crrc.entity.PositionDescriptionBasis;
import com.crrc.entity.PositionDescriptionCompetency;
import com.crrc.entity.PositionDescriptionCoordination;
import com.crrc.entity.PositionDescriptionResponsibility;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.mapper.PositionDescriptionMapper;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.pojo.dto.PositionDescriptionAuditDTO;
import com.crrc.pojo.dto.PositionDescriptionSaveDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PositionDescriptionAssessmentExportVO;
import com.crrc.pojo.vo.PositionDescriptionAttachmentExportVO;
import com.crrc.pojo.vo.PositionDescriptionAuditRecordExportVO;
import com.crrc.pojo.vo.PositionDescriptionAuthorityExportVO;
import com.crrc.pojo.vo.PositionDescriptionBasisExportVO;
import com.crrc.pojo.vo.PositionDescriptionCompetencyExportVO;
import com.crrc.pojo.vo.PositionDescriptionCoordinationExportVO;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionDescriptionExportBundleVO;
import com.crrc.pojo.vo.PositionDescriptionMainExportVO;
import com.crrc.pojo.vo.PositionDescriptionResponsibilityExportVO;
import com.crrc.pojo.vo.PositionDescriptionWorkflowRoleVO;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.service.PositionDescriptionAssessmentService;
import com.crrc.service.PositionDescriptionAttachmentService;
import com.crrc.service.PositionDescriptionAuditRecordService;
import com.crrc.service.PositionDescriptionAuthorityService;
import com.crrc.service.PositionDescriptionBasisService;
import com.crrc.service.PositionDescriptionCompetencyService;
import com.crrc.service.PositionDescriptionCoordinationService;
import com.crrc.service.PositionDescriptionResponsibilityService;
import com.crrc.service.PositionDescriptionService;
import com.crrc.utils.FlowableUtils;
import com.crrc.utils.SecurityFrameworkUtils;
import com.crrc.utils.WrapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 职位说明书 服务实现类。
 */
@Service
@Slf4j
public class PositionDescriptionServiceImpl extends WorkServiceImpl<PositionDescriptionMapper, PositionDescription> implements PositionDescriptionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PositionInfoMapper positionInfoMapper;

    @Autowired
    private PositionDescriptionResponsibilityService responsibilityService;

    @Autowired
    private PositionDescriptionBasisService basisService;

    @Autowired
    private PositionDescriptionCoordinationService coordinationService;

    @Autowired
    private PositionDescriptionAuthorityService authorityService;

    @Autowired
    private PositionDescriptionAssessmentService assessmentService;

    @Autowired
    private PositionDescriptionCompetencyService competencyService;

    @Autowired
    private PositionDescriptionAttachmentService attachmentService;

    @Autowired
    private PositionDescriptionAuditRecordService auditRecordService;

    @Autowired
    private FlowableUtils flowableUtils;

    @Override
    public void queryPage(Page<PositionDescription> page, PositionDescription queryReqVO) {
        queryReqVO = queryReqVO == null ? new PositionDescription() : queryReqVO;
        QueryWrapper<PositionDescription> wrapper = WrapperUtils.buildWrapper(queryReqVO, WrapperUtils.StringMode.LIKE);
        wrapper.last("order by update_time desc nulls last");
        this.baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PositionDescription> queryList(PositionDescription queryReqVO) {
        queryReqVO = queryReqVO == null ? new PositionDescription() : queryReqVO;
        QueryWrapper<PositionDescription> wrapper = WrapperUtils.buildWrapper(queryReqVO, WrapperUtils.StringMode.LIKE);
        wrapper.last("order by update_time desc nulls last");
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionDescriptionDetailVO saveDraft(PositionDescriptionSaveDTO request) {
        if (request == null || request.getDescription() == null) {
            throw new IllegalArgumentException("职位说明书主表不能为空");
        }
        PositionDescription description = request.getDescription();
        if (StringUtils.isBlank(description.getPositionCode())) {
            throw new IllegalArgumentException("职位编码不能为空");
        }

        PositionInfo positionInfo = requirePosition(description.getPositionCode());
        PositionDescription existing = null;
        if (description.getId() != null) {
            existing = requireDescription(description.getId());
            if (!isEditable(existing.getStatus())) {
                throw new IllegalArgumentException("当前职位说明书状态不允许编辑，状态=" + existing.getStatus());
            }
            description.setStatus(existing.getStatus());
            description.setProcInstId(existing.getProcInstId());
            description.setSubmitTime(existing.getSubmitTime());
            description.setPublishTime(existing.getPublishTime());
            description.setPublishBy(existing.getPublishBy());
            description.setLatestFlag(existing.getLatestFlag());
        } else {
            checkNoApprovingDescription(description.getPositionCode());
            description.setStatus(PositionManagementConstants.DESC_STATUS_DRAFT);
            description.setLatestFlag(0);
        }

        fillMainFieldsFromPosition(description, positionInfo);
        validateAttachments(request.getAttachments());

        if (description.getId() == null) {
            this.save(description);
        } else {
            this.updateById(description);
        }
        replaceChildren(description.getId(), request);
        syncPositionDescriptionStatus(description.getPositionCode(), description.getStatus());
        return getDetail(description.getId());
    }

    @Override
    public PositionDescriptionDetailVO getDetail(Long id) {
        PositionDescription description = requireDescription(id);
        PositionDescriptionDetailVO detail = new PositionDescriptionDetailVO();
        detail.setDescription(description);
        detail.setResponsibilities(queryResponsibilities(id));
        detail.setBasisList(queryBasis(id));
        detail.setCoordinationList(queryCoordinations(id));
        detail.setAuthorityList(queryAuthorities(id));
        detail.setAssessmentList(queryAssessments(id));
        detail.setCompetencyList(queryCompetencies(id));
        detail.setAttachments(queryAttachments(id));
        detail.setAuditRecords(queryAuditRecords(id));
        return detail;
    }

    @Override
    public PositionDescriptionDetailVO getLatestByPositionCode(String positionCode) {
        if (StringUtils.isBlank(positionCode)) {
            throw new IllegalArgumentException("职位编码不能为空");
        }
        PositionDescription published = this.baseMapper.selectOne(new LambdaQueryWrapper<PositionDescription>()
                .eq(PositionDescription::getPositionCode, positionCode)
                .eq(PositionDescription::getStatus, PositionManagementConstants.DESC_STATUS_PUBLISHED)
                .eq(PositionDescription::getLatestFlag, 1)
                .orderByDesc(PositionDescription::getPublishTime)
                .last("limit 1"));
        if (published != null) {
            return getDetail(published.getId());
        }
        PositionDescription latest = this.baseMapper.selectOne(new LambdaQueryWrapper<PositionDescription>()
                .eq(PositionDescription::getPositionCode, positionCode)
                .orderByDesc(PositionDescription::getUpdateTime)
                .last("limit 1"));
        return latest == null ? null : getDetail(latest.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionDescription submit(Long id) {
        PositionDescription description = requireDescription(id);
        if (!isEditable(description.getStatus())) {
            throw new IllegalArgumentException("只有草稿或已驳回的职位说明书可以提交审批");
        }
        validateBeforeSubmit(getDetail(id));

        StartProcessInstanceDTO instanceDTO = buildStartProcessInstanceDTO(description);
        ProcessInstanceVo processInstanceVo = flowableUtils.startProcessInstanceNew(instanceDTO);
        if (processInstanceVo == null || StringUtils.isBlank(processInstanceVo.getProcessInstanceId())) {
            throw new IllegalArgumentException("职位说明书审批流程启动失败：未返回流程实例ID");
        }

        description.setProcInstId(processInstanceVo.getProcessInstanceId());
        description.setSubmitTime(LocalDateTime.now());
        description.setStatus(PositionManagementConstants.DESC_STATUS_APPROVING);
        this.updateById(description);
        syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_APPROVING);
        return description;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(PositionDescriptionAuditDTO request) {
        PositionDescription description = requireDescriptionForAudit(request);
        String procInstId = resolveProcInstId(request, description);
        if (!PositionManagementConstants.DESC_STATUS_APPROVING.equals(description.getStatus())) {
            throw new IllegalArgumentException("当前职位说明书不是审批中状态，不能审批通过");
        }
        if (request != null && !CollectionUtils.isEmpty(request.getVariables())) {
            flowableUtils.resetVariables(procInstId, request.getVariables());
        }
        flowableUtils.done(procInstId, request == null ? null : request.getAuditOpinion());
        saveAuditRecord(description, request, PositionManagementConstants.AUDIT_RESULT_APPROVE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(PositionDescriptionAuditDTO request) {
        PositionDescription description = requireDescriptionForAudit(request);
        String procInstId = resolveProcInstId(request, description);
        if (!PositionManagementConstants.DESC_STATUS_APPROVING.equals(description.getStatus())) {
            throw new IllegalArgumentException("当前职位说明书不是审批中状态，不能驳回");
        }
        flowableUtils.reject(procInstId, request == null ? null : request.getAuditOpinion());
        description.setStatus(PositionManagementConstants.DESC_STATUS_REJECTED);
        this.updateById(description);
        syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_REJECTED);
        saveAuditRecord(description, request, PositionManagementConstants.AUDIT_RESULT_REJECT);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean workflowCallback(WorkflowWriteBackParamDTO param) {
        if (param == null) {
            throw new IllegalArgumentException("Flowable回调参数不能为空");
        }
        Long descriptionId = parseDescriptionId(param.getBizId());
        PositionDescription description = descriptionId == null ? null : this.getById(descriptionId);
        if (description == null && StringUtils.isNotBlank(param.getProcessInstanceId())) {
            description = this.baseMapper.selectOne(new LambdaQueryWrapper<PositionDescription>()
                    .eq(PositionDescription::getProcInstId, param.getProcessInstanceId())
                    .last("limit 1"));
        }
        if (description == null) {
            throw new IllegalArgumentException("职位说明书不存在，bizId=" + param.getBizId() + "，processInstanceId=" + param.getProcessInstanceId());
        }

        if (Objects.equals(PositionManagementConstants.FLOW_STATUS_FINISHED, param.getProcessInstanceStatus())) {
            return publish(description.getId());
        }
        if (Objects.equals(PositionManagementConstants.FLOW_STATUS_REJECTED, param.getProcessInstanceStatus())) {
            description.setStatus(PositionManagementConstants.DESC_STATUS_REJECTED);
            this.updateById(description);
            syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_REJECTED);
            PositionDescriptionAuditRecord record = new PositionDescriptionAuditRecord();
            record.setDescriptionId(description.getId());
            record.setProcInstId(param.getProcessInstanceId());
            record.setNodeKey(param.getNodeCode());
            record.setNodeName("Flowable回调驳回");
            record.setAuditResult(PositionManagementConstants.AUDIT_RESULT_REJECT);
            record.setAuditOpinion("Flowable流程回调：流程已拒绝");
            record.setAuditTime(LocalDateTime.now());
            auditRecordService.save(record);
            return true;
        }
        log.info("职位说明书Flowable回调未触发状态变更，descriptionId={}, processStatus={}", description.getId(), param.getProcessInstanceStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        PositionDescription description = requireDescription(id);
        if (PositionManagementConstants.DESC_STATUS_PUBLISHED.equals(description.getStatus())) {
            return true;
        }
        if (PositionManagementConstants.DESC_STATUS_DISABLED.equals(description.getStatus())) {
            throw new IllegalArgumentException("已停用的职位说明书不能发布");
        }
        if (!PositionManagementConstants.DESC_STATUS_APPROVING.equals(description.getStatus())) {
            throw new IllegalArgumentException("只有审批中的职位说明书可以发布，正常发布请通过Flowable回调触发");
        }
        this.baseMapper.update(null, new LambdaUpdateWrapper<PositionDescription>()
                .eq(PositionDescription::getPositionCode, description.getPositionCode())
                .set(PositionDescription::getLatestFlag, 0));
        description.setLatestFlag(1);
        description.setStatus(PositionManagementConstants.DESC_STATUS_PUBLISHED);
        description.setPublishTime(LocalDateTime.now());
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            description.setPublishBy(currentUserId);
        }
        this.updateById(description);
        syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_PUBLISHED);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long id) {
        PositionDescription description = requireDescription(id);
        if (PositionManagementConstants.DESC_STATUS_APPROVING.equals(description.getStatus())) {
            throw new IllegalArgumentException("审批中的职位说明书不能停用，请先终止或驳回流程");
        }
        description.setStatus(PositionManagementConstants.DESC_STATUS_DISABLED);
        description.setLatestFlag(0);
        this.updateById(description);

        PositionDescription latestPublished = this.baseMapper.selectOne(new LambdaQueryWrapper<PositionDescription>()
                .eq(PositionDescription::getPositionCode, description.getPositionCode())
                .eq(PositionDescription::getStatus, PositionManagementConstants.DESC_STATUS_PUBLISHED)
                .orderByDesc(PositionDescription::getPublishTime)
                .last("limit 1"));
        if (latestPublished != null) {
            latestPublished.setLatestFlag(1);
            this.updateById(latestPublished);
            syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_PUBLISHED);
        } else {
            syncPositionDescriptionStatus(description.getPositionCode(), PositionManagementConstants.DESC_STATUS_NONE);
        }
        return true;
    }

    @Override
    public ProcessRunningInfoVo runningSequenceGraph(Long id) {
        PositionDescription description = requireDescription(id);
        if (StringUtils.isBlank(description.getProcInstId())) {
            throw new IllegalArgumentException("该职位说明书尚未发起审批流程");
        }
        return flowableUtils.runningSequenceGraph(description.getProcInstId());
    }



    @Override
    public List<PositionDescriptionMainExportVO> exportMainList(PositionDescription queryReqVO) {
        List<PositionDescription> list = queryList(queryReqVO);
        List<PositionDescriptionMainExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        for (PositionDescription description : list) {
            if (description != null) {
                rows.add(buildMainExportRow(description));
            }
        }
        return rows;
    }

    @Override
    public PositionDescriptionExportBundleVO buildExportBundle(Long id) {
        PositionDescriptionDetailVO detail = getDetail(id);
        PositionDescriptionExportBundleVO bundle = new PositionDescriptionExportBundleVO();
        if (detail == null || detail.getDescription() == null) {
            return bundle;
        }
        PositionDescription description = detail.getDescription();
        bundle.getMainRows().add(buildMainExportRow(description));
        bundle.getResponsibilityRows().addAll(buildResponsibilityExportRows(description, detail.getResponsibilities()));
        bundle.getBasisRows().addAll(buildBasisExportRows(description, detail.getBasisList()));
        bundle.getCoordinationRows().addAll(buildCoordinationExportRows(description, detail.getCoordinationList()));
        bundle.getAuthorityRows().addAll(buildAuthorityExportRows(description, detail.getAuthorityList()));
        bundle.getAssessmentRows().addAll(buildAssessmentExportRows(description, detail.getAssessmentList()));
        bundle.getCompetencyRows().addAll(buildCompetencyExportRows(description, detail.getCompetencyList()));
        bundle.getAttachmentRows().addAll(buildAttachmentExportRows(description, detail.getAttachments()));
        bundle.getAuditRecordRows().addAll(buildAuditRecordExportRows(description, detail.getAuditRecords()));
        return bundle;
    }

    @Override
    public List<PositionDescriptionWorkflowRoleVO> workflowRoles() {
        List<PositionDescriptionWorkflowRoleVO> roles = new ArrayList<>();
        roles.add(new PositionDescriptionWorkflowRoleVO(PositionManagementConstants.NODE_POSITION_DESC_UNIT_LEADER, "各单位人事主管领导审批", PositionManagementConstants.ROLE_POSITION_DESC_UNIT_LEADER));
        roles.add(new PositionDescriptionWorkflowRoleVO(PositionManagementConstants.NODE_POSITION_DESC_SYSTEM_AUDITOR, "各体系主管部门审核人员审批", PositionManagementConstants.ROLE_POSITION_DESC_SYSTEM_AUDITOR));
        roles.add(new PositionDescriptionWorkflowRoleVO(PositionManagementConstants.NODE_POSITION_QUOTA_MANAGER, "人力资源部岗位定编管理人员审批", PositionManagementConstants.ROLE_POSITION_QUOTA_MANAGER));
        roles.add(new PositionDescriptionWorkflowRoleVO(PositionManagementConstants.NODE_HR_LEADER, "人力资源部分管领导审批", PositionManagementConstants.ROLE_HR_LEADER));
        return roles;
    }


    private PositionDescriptionMainExportVO buildMainExportRow(PositionDescription description) {
        PositionDescriptionMainExportVO row = new PositionDescriptionMainExportVO();
        row.setDescriptionId(description.getId());
        row.setPositionCode(description.getPositionCode());
        row.setPositionName(description.getPositionName());
        row.setVersion(description.getVersion());
        row.setEffectiveDate(formatDate(description.getEffectiveDate()));
        row.setRevocationDate(formatDate(description.getRevocationDate()));
        row.setOrgUnit(description.getOrgUnit());
        row.setJobCategory(description.getJobCategory());
        row.setJobGroup(description.getJobGroup());
        row.setJobType(description.getJobType());
        row.setJobLevel(description.getJobLevel());
        row.setWorkPlace(description.getWorkPlace());
        row.setWorkContent(description.getWorkContent());
        row.setJobGrade(description.getJobGrade());
        row.setHeadcountQuota(description.getHeadcountQuota());
        row.setControlMode(description.getControlMode());
        row.setDirectSuperiorPositionCode(description.getDirectSuperiorPositionCode());
        row.setDirectSuperiorPositionName(description.getDirectSuperiorPositionName());
        row.setDirectSubordinatePositionCodes(description.getDirectSubordinatePositionCodes());
        row.setDirectSubordinatePositionNames(description.getDirectSubordinatePositionNames());
        row.setPositionPurpose(description.getPositionPurpose());
        row.setWorkEnvironment(description.getWorkEnvironment());
        row.setQualification(description.getQualification());
        row.setRequiredKnowledge(description.getRequiredKnowledge());
        row.setStatus(descriptionStatusLabel(description.getStatus()));
        row.setProcInstId(description.getProcInstId());
        row.setSubmitTime(formatDateTime(description.getSubmitTime()));
        row.setPublishTime(formatDateTime(description.getPublishTime()));
        row.setLatestFlag(latestFlagLabel(description.getLatestFlag()));
        return row;
    }

    private List<PositionDescriptionResponsibilityExportVO> buildResponsibilityExportRows(PositionDescription description, List<PositionDescriptionResponsibility> list) {
        List<PositionDescriptionResponsibilityExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionResponsibility item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionResponsibilityExportVO row = new PositionDescriptionResponsibilityExportVO();
            fillCommonExportFields(row, description, serialNo++);
            row.setResponsibilityName(item.getResponsibilityName());
            row.setResponsibilityDesc(item.getResponsibilityDesc());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionBasisExportVO> buildBasisExportRows(PositionDescription description, List<PositionDescriptionBasis> list) {
        List<PositionDescriptionBasisExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionBasis item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionBasisExportVO row = new PositionDescriptionBasisExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setBasisContent(item.getBasisContent());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionCoordinationExportVO> buildCoordinationExportRows(PositionDescription description, List<PositionDescriptionCoordination> list) {
        List<PositionDescriptionCoordinationExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionCoordination item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionCoordinationExportVO row = new PositionDescriptionCoordinationExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setCoordinationType(item.getCoordinationType());
            row.setCoordinationObject(item.getCoordinationObject());
            row.setCoordinationContent(item.getCoordinationContent());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionAuthorityExportVO> buildAuthorityExportRows(PositionDescription description, List<PositionDescriptionAuthority> list) {
        List<PositionDescriptionAuthorityExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionAuthority item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionAuthorityExportVO row = new PositionDescriptionAuthorityExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setAuthorityContent(item.getAuthorityContent());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionAssessmentExportVO> buildAssessmentExportRows(PositionDescription description, List<PositionDescriptionAssessment> list) {
        List<PositionDescriptionAssessmentExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionAssessment item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionAssessmentExportVO row = new PositionDescriptionAssessmentExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setAssessmentContent(item.getAssessmentContent());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionCompetencyExportVO> buildCompetencyExportRows(PositionDescription description, List<PositionDescriptionCompetency> list) {
        List<PositionDescriptionCompetencyExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionCompetency item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionCompetencyExportVO row = new PositionDescriptionCompetencyExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setCompetencyCategory(item.getCompetencyCategory());
            row.setCompetencyName(item.getCompetencyName());
            row.setCompetencyLevel(item.getCompetencyLevel());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionAttachmentExportVO> buildAttachmentExportRows(PositionDescription description, List<PositionDescriptionAttachment> list) {
        List<PositionDescriptionAttachmentExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionAttachment item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionAttachmentExportVO row = new PositionDescriptionAttachmentExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setFileId(item.getFileId());
            row.setFileName(item.getFileName());
            row.setFileType(item.getFileType());
            rows.add(row);
        }
        return rows;
    }

    private List<PositionDescriptionAuditRecordExportVO> buildAuditRecordExportRows(PositionDescription description, List<PositionDescriptionAuditRecord> list) {
        List<PositionDescriptionAuditRecordExportVO> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return rows;
        }
        int serialNo = 1;
        for (PositionDescriptionAuditRecord item : list) {
            if (item == null) {
                continue;
            }
            PositionDescriptionAuditRecordExportVO row = new PositionDescriptionAuditRecordExportVO();
            row.setSerialNo(serialNo++);
            row.setPositionCode(description.getPositionCode());
            row.setPositionName(description.getPositionName());
            row.setNodeKey(item.getNodeKey());
            row.setNodeName(item.getNodeName());
            row.setRoleCode(item.getRoleCode());
            row.setAuditorCode(item.getAuditorCode());
            row.setAuditorName(item.getAuditorName());
            row.setAuditResult(auditResultLabel(item.getAuditResult()));
            row.setAuditOpinion(item.getAuditOpinion());
            row.setAuditTime(formatDateTime(item.getAuditTime()));
            rows.add(row);
        }
        return rows;
    }

    private void fillCommonExportFields(PositionDescriptionResponsibilityExportVO row, PositionDescription description, int serialNo) {
        row.setSerialNo(serialNo);
        row.setPositionCode(description.getPositionCode());
        row.setPositionName(description.getPositionName());
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMATTER.format(date);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String latestFlagLabel(Integer latestFlag) {
        if (latestFlag == null) {
            return null;
        }
        return latestFlag == 1 ? "是" : "否";
    }

    private String auditResultLabel(String result) {
        if (StringUtils.isBlank(result)) {
            return result;
        }
        if (PositionManagementConstants.AUDIT_RESULT_APPROVE.equals(result)) {
            return "通过";
        }
        if (PositionManagementConstants.AUDIT_RESULT_REJECT.equals(result)) {
            return "驳回";
        }
        return result;
    }

    private String descriptionStatusLabel(String status) {
        if (StringUtils.isBlank(status)) {
            return status;
        }
        if (PositionManagementConstants.DESC_STATUS_NONE.equals(status)) {
            return "无说明书";
        }
        if (PositionManagementConstants.DESC_STATUS_DRAFT.equals(status)) {
            return "草稿";
        }
        if (PositionManagementConstants.DESC_STATUS_APPROVING.equals(status)) {
            return "审批中";
        }
        if (PositionManagementConstants.DESC_STATUS_REJECTED.equals(status)) {
            return "已驳回";
        }
        if (PositionManagementConstants.DESC_STATUS_PUBLISHED.equals(status)) {
            return "已发布";
        }
        if (PositionManagementConstants.DESC_STATUS_DISABLED.equals(status)) {
            return "已停用";
        }
        return status;
    }

    private StartProcessInstanceDTO buildStartProcessInstanceDTO(PositionDescription description) {
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        instanceDTO.setBizCode("PD-" + description.getPositionCode() + "-" + System.currentTimeMillis());
        instanceDTO.setBizId(PositionManagementConstants.POSITION_DESCRIPTION_BIZ_ID_PREFIX + description.getId());
        instanceDTO.setTemplateAlias(PositionManagementConstants.POSITION_DESCRIPTION_APPROVAL_PROCESS_KEY);

        Map<String, Object> formData = new HashMap<>();
        formData.put(PositionManagementConstants.FLOW_VAR_TYPE, PositionManagementConstants.FLOW_TYPE_POSITION_DESCRIPTION);
        formData.put("descriptionId", description.getId());
        formData.put("positionCode", description.getPositionCode());
        formData.put("positionName", description.getPositionName());
        formData.put("orgUnit", description.getOrgUnit());
        formData.put("jobCategory", description.getJobCategory());
        formData.put("jobGroup", description.getJobGroup());
        formData.put("jobType", description.getJobType());
        formData.put("version", description.getVersion());
        formData.put("roleUnitLeader", PositionManagementConstants.ROLE_POSITION_DESC_UNIT_LEADER);
        formData.put("roleSystemAuditor", PositionManagementConstants.ROLE_POSITION_DESC_SYSTEM_AUDITOR);
        formData.put("rolePositionQuotaManager", PositionManagementConstants.ROLE_POSITION_QUOTA_MANAGER);
        formData.put("roleHrLeader", PositionManagementConstants.ROLE_HR_LEADER);
        formData.put("nodeUnitLeader", PositionManagementConstants.NODE_POSITION_DESC_UNIT_LEADER);
        formData.put("nodeSystemAuditor", PositionManagementConstants.NODE_POSITION_DESC_SYSTEM_AUDITOR);
        formData.put("nodePositionQuotaManager", PositionManagementConstants.NODE_POSITION_QUOTA_MANAGER);
        formData.put("nodeHrLeader", PositionManagementConstants.NODE_HR_LEADER);
        instanceDTO.setFormData(formData);
        return instanceDTO;
    }

    private void replaceChildren(Long descriptionId, PositionDescriptionSaveDTO request) {
        replaceResponsibilities(descriptionId, request.getResponsibilities());
        replaceBasis(descriptionId, request.getBasisList());
        replaceCoordinations(descriptionId, request.getCoordinationList());
        replaceAuthorities(descriptionId, request.getAuthorityList());
        replaceAssessments(descriptionId, request.getAssessmentList());
        replaceCompetencies(descriptionId, request.getCompetencyList());
        replaceAttachments(descriptionId, request.getAttachments());
    }

    private void replaceResponsibilities(Long descriptionId, List<PositionDescriptionResponsibility> list) {
        responsibilityService.remove(new LambdaQueryWrapper<PositionDescriptionResponsibility>().eq(PositionDescriptionResponsibility::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionResponsibility item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        responsibilityService.saveBatch(list);
    }

    private void replaceBasis(Long descriptionId, List<PositionDescriptionBasis> list) {
        basisService.remove(new LambdaQueryWrapper<PositionDescriptionBasis>().eq(PositionDescriptionBasis::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionBasis item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        basisService.saveBatch(list);
    }

    private void replaceCoordinations(Long descriptionId, List<PositionDescriptionCoordination> list) {
        coordinationService.remove(new LambdaQueryWrapper<PositionDescriptionCoordination>().eq(PositionDescriptionCoordination::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionCoordination item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        coordinationService.saveBatch(list);
    }

    private void replaceAuthorities(Long descriptionId, List<PositionDescriptionAuthority> list) {
        authorityService.remove(new LambdaQueryWrapper<PositionDescriptionAuthority>().eq(PositionDescriptionAuthority::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionAuthority item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        authorityService.saveBatch(list);
    }

    private void replaceAssessments(Long descriptionId, List<PositionDescriptionAssessment> list) {
        assessmentService.remove(new LambdaQueryWrapper<PositionDescriptionAssessment>().eq(PositionDescriptionAssessment::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionAssessment item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        assessmentService.saveBatch(list);
    }

    private void replaceCompetencies(Long descriptionId, List<PositionDescriptionCompetency> list) {
        competencyService.remove(new LambdaQueryWrapper<PositionDescriptionCompetency>().eq(PositionDescriptionCompetency::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionCompetency item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        competencyService.saveBatch(list);
    }

    private void replaceAttachments(Long descriptionId, List<PositionDescriptionAttachment> list) {
        attachmentService.remove(new LambdaQueryWrapper<PositionDescriptionAttachment>().eq(PositionDescriptionAttachment::getDescriptionId, descriptionId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int sort = 1;
        for (PositionDescriptionAttachment item : list) {
            item.setId(null);
            item.setDescriptionId(descriptionId);
            item.setDeleted(0);
            if (item.getSortNo() == null) {
                item.setSortNo(sort);
            }
            sort++;
        }
        attachmentService.saveBatch(list);
    }

    private List<PositionDescriptionResponsibility> queryResponsibilities(Long descriptionId) {
        return responsibilityService.list(new LambdaQueryWrapper<PositionDescriptionResponsibility>()
                .eq(PositionDescriptionResponsibility::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionResponsibility::getSortNo));
    }

    private List<PositionDescriptionBasis> queryBasis(Long descriptionId) {
        return basisService.list(new LambdaQueryWrapper<PositionDescriptionBasis>()
                .eq(PositionDescriptionBasis::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionBasis::getSortNo));
    }

    private List<PositionDescriptionCoordination> queryCoordinations(Long descriptionId) {
        return coordinationService.list(new LambdaQueryWrapper<PositionDescriptionCoordination>()
                .eq(PositionDescriptionCoordination::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionCoordination::getSortNo));
    }

    private List<PositionDescriptionAuthority> queryAuthorities(Long descriptionId) {
        return authorityService.list(new LambdaQueryWrapper<PositionDescriptionAuthority>()
                .eq(PositionDescriptionAuthority::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionAuthority::getSortNo));
    }

    private List<PositionDescriptionAssessment> queryAssessments(Long descriptionId) {
        return assessmentService.list(new LambdaQueryWrapper<PositionDescriptionAssessment>()
                .eq(PositionDescriptionAssessment::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionAssessment::getSortNo));
    }

    private List<PositionDescriptionCompetency> queryCompetencies(Long descriptionId) {
        return competencyService.list(new LambdaQueryWrapper<PositionDescriptionCompetency>()
                .eq(PositionDescriptionCompetency::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionCompetency::getSortNo));
    }

    private List<PositionDescriptionAttachment> queryAttachments(Long descriptionId) {
        return attachmentService.list(new LambdaQueryWrapper<PositionDescriptionAttachment>()
                .eq(PositionDescriptionAttachment::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionAttachment::getSortNo));
    }

    private List<PositionDescriptionAuditRecord> queryAuditRecords(Long descriptionId) {
        return auditRecordService.list(new LambdaQueryWrapper<PositionDescriptionAuditRecord>()
                .eq(PositionDescriptionAuditRecord::getDescriptionId, descriptionId)
                .orderByAsc(PositionDescriptionAuditRecord::getAuditTime));
    }

    private PositionDescription requireDescription(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("职位说明书ID不能为空");
        }
        PositionDescription description = this.getById(id);
        if (description == null) {
            throw new IllegalArgumentException("职位说明书不存在或已删除，id=" + id);
        }
        return description;
    }

    private PositionDescription requireDescriptionForAudit(PositionDescriptionAuditDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("审批请求不能为空");
        }
        if (request.getId() != null) {
            return requireDescription(request.getId());
        }
        if (StringUtils.isNotBlank(request.getProcInstId())) {
            PositionDescription description = this.baseMapper.selectOne(new LambdaQueryWrapper<PositionDescription>()
                    .eq(PositionDescription::getProcInstId, request.getProcInstId())
                    .last("limit 1"));
            if (description != null) {
                return description;
            }
        }
        throw new IllegalArgumentException("职位说明书ID或流程实例ID不能为空");
    }

    private String resolveProcInstId(PositionDescriptionAuditDTO request, PositionDescription description) {
        String procInstId = request == null ? null : request.getProcInstId();
        if (StringUtils.isBlank(procInstId)) {
            procInstId = description.getProcInstId();
        }
        if (StringUtils.isBlank(procInstId)) {
            throw new IllegalArgumentException("Flowable流程实例ID不能为空");
        }
        return procInstId;
    }

    private PositionInfo requirePosition(String positionCode) {
        List<PositionInfo> list = positionInfoMapper.selectList(new LambdaQueryWrapper<PositionInfo>()
                .eq(PositionInfo::getCode, positionCode)
                .last("limit 2"));
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalArgumentException("职位不存在，职位编码=" + positionCode);
        }
        if (list.size() > 1) {
            throw new IllegalArgumentException("职位编码存在重复数据，请先清理：" + positionCode);
        }
        return list.get(0);
    }

    private void fillMainFieldsFromPosition(PositionDescription description, PositionInfo positionInfo) {
        if (StringUtils.isBlank(description.getPositionName())) {
            description.setPositionName(positionInfo.getName());
        }
        if (StringUtils.isBlank(description.getVersion())) {
            description.setVersion(StringUtils.defaultIfBlank(positionInfo.getVersion(), "1.0"));
        }
        if (description.getEffectiveDate() == null) {
            description.setEffectiveDate(positionInfo.getEffectiveDate());
        }
        if (description.getRevocationDate() == null) {
            description.setRevocationDate(positionInfo.getRevocationDate());
        }
        if (StringUtils.isBlank(description.getOrgUnit())) {
            description.setOrgUnit(positionInfo.getOrgUnit());
        }
        if (StringUtils.isBlank(description.getJobCategory())) {
            description.setJobCategory(positionInfo.getJobCategory());
        }
        if (StringUtils.isBlank(description.getJobGroup())) {
            description.setJobGroup(positionInfo.getJobGroup());
        }
        if (StringUtils.isBlank(description.getJobType())) {
            description.setJobType(positionInfo.getJobType());
        }
        if (StringUtils.isBlank(description.getJobLevel())) {
            description.setJobLevel(positionInfo.getJobLevel());
        }
        if (StringUtils.isBlank(description.getWorkPlace())) {
            description.setWorkPlace(positionInfo.getWorkPlace());
        }
        if (StringUtils.isBlank(description.getWorkContent())) {
            description.setWorkContent(positionInfo.getWorkContent());
        }
        if (StringUtils.isBlank(description.getJobGrade())) {
            description.setJobGrade(positionInfo.getJobGrade());
        }
        if (description.getHeadcountQuota() == null) {
            description.setHeadcountQuota(positionInfo.getHeadcountQuota());
        }
        if (StringUtils.isBlank(description.getControlMode())) {
            description.setControlMode(positionInfo.getControlMode());
        }
        if (StringUtils.isBlank(description.getDirectSuperiorPositionCode())) {
            description.setDirectSuperiorPositionCode(positionInfo.getParentPositionCode());
        }
        if (StringUtils.isBlank(description.getDirectSuperiorPositionName())) {
            description.setDirectSuperiorPositionName(positionInfo.getParentPositionName());
        }
        fillDirectSubordinateIfBlank(description);
        if (StringUtils.isBlank(description.getStatus())) {
            description.setStatus(PositionManagementConstants.DESC_STATUS_DRAFT);
        }
        if (description.getLatestFlag() == null) {
            description.setLatestFlag(0);
        }
        if (description.getDeleted() == null) {
            description.setDeleted(0);
        }
    }

    private void fillDirectSubordinateIfBlank(PositionDescription description) {
        if (StringUtils.isNotBlank(description.getDirectSubordinatePositionCodes())
                && StringUtils.isNotBlank(description.getDirectSubordinatePositionNames())) {
            return;
        }
        List<PositionInfo> children = positionInfoMapper.selectList(new LambdaQueryWrapper<PositionInfo>()
                .eq(PositionInfo::getParentPositionCode, description.getPositionCode())
                .orderByAsc(PositionInfo::getCode));
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        if (StringUtils.isBlank(description.getDirectSubordinatePositionCodes())) {
            description.setDirectSubordinatePositionCodes(children.stream()
                    .map(PositionInfo::getCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(",")));
        }
        if (StringUtils.isBlank(description.getDirectSubordinatePositionNames())) {
            description.setDirectSubordinatePositionNames(children.stream()
                    .map(PositionInfo::getName)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(",")));
        }
    }

    private void validateBeforeSubmit(PositionDescriptionDetailVO detail) {
        PositionDescription description = detail.getDescription();
        List<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(description.getPositionCode())) {
            errors.add("职位编码不能为空");
        }
        if (StringUtils.isBlank(description.getPositionName())) {
            errors.add("职位名称不能为空");
        }
        if (StringUtils.isBlank(description.getVersion())) {
            errors.add("版本不能为空");
        }
        if (description.getEffectiveDate() == null) {
            errors.add("生效日期不能为空");
        }
        if (StringUtils.isBlank(description.getOrgUnit())) {
            errors.add("所属组织单元不能为空");
        }
        if (StringUtils.isBlank(description.getJobType())) {
            errors.add("职种不能为空");
        }
        if (StringUtils.isBlank(description.getPositionPurpose())) {
            errors.add("职位定位不能为空");
        }
        if (CollectionUtils.isEmpty(detail.getResponsibilities())) {
            errors.add("职位职责至少填写一条");
        }
        validateAttachments(detail.getAttachments());
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("职位说明书提交校验失败：" + String.join("；", errors));
        }
    }

    private void validateAttachments(List<PositionDescriptionAttachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        for (PositionDescriptionAttachment attachment : attachments) {
            if (attachment != null && StringUtils.isBlank(attachment.getFileId())) {
                throw new IllegalArgumentException("附件fileId不能为空，文件名=" + attachment.getFileName());
            }
        }
    }

    private boolean isEditable(String status) {
        return StringUtils.isBlank(status)
                || PositionManagementConstants.DESC_STATUS_DRAFT.equals(status)
                || PositionManagementConstants.DESC_STATUS_REJECTED.equals(status);
    }

    private void checkNoApprovingDescription(String positionCode) {
        List<PositionDescription> approvingList = this.baseMapper.selectList(new LambdaQueryWrapper<PositionDescription>()
                .eq(PositionDescription::getPositionCode, positionCode)
                .eq(PositionDescription::getStatus, PositionManagementConstants.DESC_STATUS_APPROVING)
                .last("limit 1"));
        if (!CollectionUtils.isEmpty(approvingList)) {
            throw new IllegalArgumentException("该职位存在审批中的职位说明书，请审批结束后再新增版本，职位编码=" + positionCode);
        }
    }

    private void saveAuditRecord(PositionDescription description, PositionDescriptionAuditDTO request, String result) {
        PositionDescriptionAuditRecord record = new PositionDescriptionAuditRecord();
        record.setDescriptionId(description.getId());
        record.setProcInstId(resolveProcInstId(request, description));
        record.setTaskId(request == null ? null : request.getTaskId());
        record.setNodeKey(request == null ? null : request.getNodeKey());
        record.setNodeName(request == null ? null : request.getNodeName());
        record.setRoleCode(request == null ? null : request.getRoleCode());
        record.setAuditorCode(resolveAuditorCode(request));
        record.setAuditorName(resolveAuditorName(request));
        record.setAuditResult(result);
        record.setAuditOpinion(request == null ? null : request.getAuditOpinion());
        record.setAuditTime(LocalDateTime.now());
        auditRecordService.save(record);
    }

    private String resolveAuditorCode(PositionDescriptionAuditDTO request) {
        if (request != null && StringUtils.isNotBlank(request.getAuditorCode())) {
            return request.getAuditorCode();
        }
        try {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            return loginUser == null || loginUser.getUser() == null ? null : loginUser.getUser().getJobNo();
        } catch (Exception e) {
            log.warn("获取当前登录人工号失败", e);
            return null;
        }
    }

    private String resolveAuditorName(PositionDescriptionAuditDTO request) {
        if (request != null && StringUtils.isNotBlank(request.getAuditorName())) {
            return request.getAuditorName();
        }
        try {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            return loginUser == null || loginUser.getUser() == null ? null : loginUser.getUser().getUserName();
        } catch (Exception e) {
            log.warn("获取当前登录人名称失败", e);
            return null;
        }
    }

    private Long getCurrentUserId() {
        try {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().getId() == null) {
                return null;
            }
            Object userId = loginUser.getUser().getId();
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            return Long.valueOf(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("获取当前登录用户ID失败", e);
            return null;
        }
    }

    private void syncPositionDescriptionStatus(String positionCode, String status) {
        if (StringUtils.isBlank(positionCode) || StringUtils.isBlank(status)) {
            return;
        }
        positionInfoMapper.update(null, new LambdaUpdateWrapper<PositionInfo>()
                .eq(PositionInfo::getCode, positionCode)
                .set(PositionInfo::getPositionDescStatus, status));
    }

    private Long parseDescriptionId(String bizId) {
        if (StringUtils.isBlank(bizId)) {
            return null;
        }
        String raw = bizId.trim();
        if (raw.startsWith(PositionManagementConstants.POSITION_DESCRIPTION_BIZ_ID_PREFIX)) {
            raw = raw.substring(PositionManagementConstants.POSITION_DESCRIPTION_BIZ_ID_PREFIX.length());
        } else if (raw.contains("_")) {
            raw = raw.substring(raw.lastIndexOf('_') + 1);
        }
        if (!raw.matches("\\d+")) {
            return null;
        }
        return Long.valueOf(raw);
    }
}
