package com.crrc.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.constants.PositionManagementConstants;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.pojo.dto.PositionInfoCopyRequest;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionIncumbentDTO;
import com.crrc.pojo.vo.PositionInfoBasicExportVO;
import com.crrc.pojo.vo.PositionInfoDescriptionExportVO;
import com.crrc.pojo.vo.PositionInfoFullImportVO;
import com.crrc.pojo.vo.PositionInfoImportResultVO;
import com.crrc.service.PositionDescriptionService;
import com.crrc.service.PositionInfoService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.PositionInfoFullImportListener;
import com.crrc.utils.PositionInfoImportListener;
import com.crrc.utils.WrapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PositionInfoServiceImpl extends WorkServiceImpl<PositionInfoMapper, PositionInfo> implements PositionInfoService {

    private static final Logger log = LoggerFactory.getLogger(PositionInfoServiceImpl.class);
    private static final int MAX_IMPORT_ERROR_COUNT = 50;
    /**
     * 列表展示时实时补全在岗人员的最大职位数量。
     * /positionInfo/query 允许外部空条件全量调用，数据量较大时不能再发起超长 IN 查询，
     * 否则会拖慢外部接口甚至触发数据库 read timeout。
     */
    private static final int MAX_REALTIME_INCUMBENT_RESOLVE_COUNT = 500;
    /**
     * 显式刷新/删除校验等必须实时查询的场景，按小批量拆分 IN 条件，避免单条 SQL 参数过多。
     */
    private static final int INCUMBENT_QUERY_BATCH_SIZE = 300;
    private static final DateTimeFormatter DATE_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATE_FORMATTER_3 = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    PostCatalogInfoServiceImpl postCatalogInfoService;

    @Autowired
    private PositionDescriptionService positionDescriptionService;

    @Override
    public void resolveField(List<PositionInfo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Map<String, String> translateMap = postCatalogInfoService.query(new PostCatalogInfo()).stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getCode()))
                .collect(Collectors.toMap(
                        PostCatalogInfo::getCode,
                        item -> StringUtils.defaultIfBlank(item.getName(), item.getCode()),
                        (a, b) -> a
                ));

        Map<String, String> deptTranslateMap = DeptUtils.getDeptTranslateMap();
        fillIncumbentFields(records);

        records.forEach(position -> {
            if (position == null) {
                return;
            }

            String jobType = StringUtils.trimToEmpty(position.getJobType());
            if (StringUtils.isNotBlank(jobType)) {
                String[] split = jobType.split("-");
                if (split.length == 3) {
                    position.setJobCategoryValue(translateMap.getOrDefault(split[0], split[0]));
                    String groupCode = String.join("-", split[0], split[1]);
                    position.setJobGroupValue(translateMap.getOrDefault(groupCode, groupCode));
                    position.setJobTypeValue(translateMap.getOrDefault(jobType, jobType));
                } else {
                    position.setJobTypeValue(translateMap.getOrDefault(jobType, jobType));
                }
            }

            if (StringUtils.isNotBlank(position.getJobCategory())) {
                position.setJobCategoryValue(translateMap.getOrDefault(position.getJobCategory(), position.getJobCategory()));
            }
            if (StringUtils.isNotBlank(position.getJobGroup())) {
                position.setJobGroupValue(translateMap.getOrDefault(position.getJobGroup(), position.getJobGroup()));
            }
            if (StringUtils.isNotBlank(position.getJobType())) {
                position.setJobTypeValue(translateMap.getOrDefault(position.getJobType(), position.getJobType()));
            }

            if (StringUtils.isNotBlank(position.getOrgUnit())) {
                position.setOrgUnitValue(deptTranslateMap.getOrDefault(position.getOrgUnit(), position.getOrgUnit()));
            }
        });
    }

    @Override
    public void queryByOrder(Page<PositionInfo> page, PositionInfo pageReqVO) {
        QueryWrapper<PositionInfo> wrapper = WrapperUtils.buildWrapper(pageReqVO, WrapperUtils.StringMode.LIKE);

        wrapper.eq(StringUtils.isNotEmpty(pageReqVO.getOrgUnit()), "org_unit", pageReqVO.getOrgUnit());
        wrapper.eq(StringUtils.isNotEmpty(pageReqVO.getPositionStatus()), "position_status", pageReqVO.getPositionStatus());
        wrapper.eq(StringUtils.isNotEmpty(pageReqVO.getPositionDescStatus()), "position_desc_status", pageReqVO.getPositionDescStatus());
        wrapper.last("order by update_time desc nulls last");
        baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PositionInfo> selectByCodes(List<String> codes) {
        List<String> normalizedCodes = normalizeCodes(codes);
        if (normalizedCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new QueryWrapper<PositionInfo>().in("code", normalizedCodes));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importJobGrade(MultipartFile file) throws Exception {
        EasyExcelFactory.read(file.getInputStream(), PositionInfo.class,
                        new PositionInfoImportListener(this))
                .headRowNumber(1)
                .sheet()
                .doRead();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionInfoImportResultVO importFull(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        PositionInfoFullImportListener listener = new PositionInfoFullImportListener();
        EasyExcelFactory.read(file.getInputStream(), PositionInfoFullImportVO.class, listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
        return importFullRows(listener.getRows());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePositionInfoWithIncumbentCheck(Collection<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        List<PositionInfo> positions = baseMapper.selectBatchIds(idList);
        if (positions == null || positions.isEmpty()) {
            return true;
        }

        List<String> codes = positions.stream()
                .map(PositionInfo::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<PositionIncumbentDTO>> incumbentMap = queryIncumbentMap(codes);

        List<String> blocked = new ArrayList<>();
        for (PositionInfo position : positions) {
            List<PositionIncumbentDTO> incumbents = incumbentMap.get(position.getCode());
            if (incumbents != null && !incumbents.isEmpty()) {
                blocked.add(position.getCode() + "(" + StringUtils.defaultIfBlank(position.getName(), "未命名") + ")：" + joinIncumbentPersons(incumbents));
            }
        }
        if (!blocked.isEmpty()) {
            throw new IllegalArgumentException("以下职位存在在岗人员，不能删除：" + String.join("；", blocked));
        }
        return removeBatchByIds(idList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshIncumbents(Collection<String> codes) {
        List<PositionInfo> positions;
        List<String> normalizedCodes = normalizeCodes(codes);
        if (normalizedCodes.isEmpty()) {
            positions = baseMapper.selectList(new QueryWrapper<PositionInfo>()
                    .select("id", "code")
                    .isNotNull("code"));
        } else {
            positions = baseMapper.selectList(new QueryWrapper<PositionInfo>()
                    .select("id", "code")
                    .in("code", normalizedCodes));
        }
        if (positions == null || positions.isEmpty()) {
            return 0;
        }

        List<String> positionCodes = positions.stream()
                .map(PositionInfo::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<PositionIncumbentDTO>> incumbentMap = queryIncumbentMap(positionCodes);

        List<PositionInfo> updates = new ArrayList<>();
        for (PositionInfo position : positions) {
            List<PositionIncumbentDTO> incumbents = incumbentMap.getOrDefault(position.getCode(), Collections.emptyList());
            PositionInfo update = new PositionInfo();
            update.setId(position.getId());
            update.setIncumbentCount(incumbents.size());
            update.setIncumbentPersons(joinIncumbentPersons(incumbents));
            updates.add(update);
        }
        updateBatchById(updates);
        return updates.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PositionInfo> copyToOrgUnit(PositionInfoCopyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("复制请求不能为空");
        }
        if (StringUtils.isBlank(request.getTargetOrgUnit())) {
            throw new IllegalArgumentException("目标组织单元不能为空");
        }

        List<PositionInfo> sources = queryCopySources(request);
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("未查询到需要复制的职位");
        }

        Set<String> usedNewCodes = new HashSet<>();
        List<PositionInfo> copies = new ArrayList<>();
        boolean keepParentRelation = Boolean.TRUE.equals(request.getKeepParentRelation());
        for (PositionInfo source : sources) {
            String newCode = resolveCopyCode(source.getCode(), request.getNewCodeMap(), usedNewCodes);
            PositionInfo target = new PositionInfo();
            BeanUtils.copyProperties(source, target);
            target.setId(null);
            target.setCode(newCode);
            target.setOrgUnit(StringUtils.trim(request.getTargetOrgUnit()));
            if (StringUtils.isNotBlank(request.getTargetDeptCode())) {
                target.setDeptCode(StringUtils.trim(request.getTargetDeptCode()));
            }
            if (StringUtils.isNotBlank(request.getTargetDeptName())) {
                target.setDeptName(StringUtils.trim(request.getTargetDeptName()));
            }
            if (!keepParentRelation) {
                target.setParentPositionCode(null);
                target.setParentPositionName(null);
            }
            target.setIncumbentCount(0);
            target.setIncumbentPersons(null);
            target.setPositionDescStatus(PositionManagementConstants.DESC_STATUS_NONE);
            target.setPositionStatus(PositionManagementConstants.POSITION_STATUS_ENABLED);
            target.setRevocationDate(null);
            copies.add(target);
        }

        saveBatch(copies);
        return copies;
    }


    @Override
    public List<PositionInfoBasicExportVO> exportBasic(PositionInfo queryReqVO) {
        queryReqVO = queryReqVO == null ? new PositionInfo() : queryReqVO;
        List<PositionInfo> positions = query(queryReqVO);
        resolveField(positions);
        List<PositionInfoBasicExportVO> rows = new ArrayList<>();
        int serialNo = 1;
        for (PositionInfo position : positions) {
            rows.add(buildBasicExportRow(position, serialNo++));
        }
        return rows;
    }

    @Override
    public List<PositionInfoDescriptionExportVO> exportWithDescription(PositionInfo queryReqVO) {
        queryReqVO = queryReqVO == null ? new PositionInfo() : queryReqVO;
        List<PositionInfo> positions = query(queryReqVO);
        resolveField(positions);
        List<PositionInfoDescriptionExportVO> rows = new ArrayList<>();
        int serialNo = 1;
        for (PositionInfo position : positions) {
            PositionDescriptionDetailVO detail = null;
            if (position != null && StringUtils.isNotBlank(position.getCode())) {
                detail = positionDescriptionService.getLatestByPositionCode(position.getCode());
            }
            rows.add(buildDescriptionExportRow(position, detail, serialNo++));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateWithValidation(PositionInfo positionInfo) {
        if (positionInfo == null) {
            throw new IllegalArgumentException("职位信息不能为空");
        }
        if (positionInfo.getId() != null && StringUtils.isBlank(positionInfo.getCode())) {
            PositionInfo dbPosition = getById(positionInfo.getId());
            if (dbPosition != null) {
                positionInfo.setCode(dbPosition.getCode());
            }
        }
        normalizePosition(positionInfo, true);
        if (StringUtils.isBlank(positionInfo.getCode())) {
            throw new IllegalArgumentException("职位编码不能为空");
        }
        validateCodeUnique(positionInfo.getCode(), positionInfo.getId());
        return saveOrUpdate(positionInfo);
    }


    private PositionInfoBasicExportVO buildBasicExportRow(PositionInfo position, int serialNo) {
        PositionInfoBasicExportVO row = new PositionInfoBasicExportVO();
        if (position == null) {
            row.setSerialNo(serialNo);
            return row;
        }
        row.setSerialNo(serialNo);
        row.setCode(position.getCode());
        row.setName(position.getName());
        row.setVersion(position.getVersion());
        row.setEffectiveDate(formatDate(position.getEffectiveDate()));
        row.setRevocationDate(formatDate(position.getRevocationDate()));
        row.setOrgUnit(position.getOrgUnit());
        row.setOrgUnitValue(position.getOrgUnitValue());
        row.setWorkCenter(position.getWorkCenter());
        row.setJobCategory(position.getJobCategory());
        row.setJobCategoryValue(position.getJobCategoryValue());
        row.setJobGroup(position.getJobGroup());
        row.setJobGroupValue(position.getJobGroupValue());
        row.setJobType(position.getJobType());
        row.setJobTypeValue(position.getJobTypeValue());
        row.setJobLevel(position.getJobLevel());
        row.setJobGrade(position.getJobGrade());
        row.setHeadcountQuota(position.getHeadcountQuota());
        row.setControlMode(position.getControlMode());
        row.setIncumbentCount(position.getIncumbentCount());
        row.setIncumbentPersons(position.getIncumbentPersons());
        row.setDeptCode(position.getDeptCode());
        row.setDeptName(position.getDeptName());
        row.setParentPositionCode(position.getParentPositionCode());
        row.setParentPositionName(position.getParentPositionName());
        row.setRelatedPost(position.getRelatedPost());
        row.setWorkPlace(position.getWorkPlace());
        row.setWorkContent(position.getWorkContent());
        row.setPositionStatus(positionStatusLabel(position.getPositionStatus()));
        row.setPositionDescStatus(descriptionStatusLabel(position.getPositionDescStatus()));
        return row;
    }

    private PositionInfoDescriptionExportVO buildDescriptionExportRow(PositionInfo position, PositionDescriptionDetailVO detail, int serialNo) {
        PositionInfoDescriptionExportVO row = new PositionInfoDescriptionExportVO();
        row.setSerialNo(serialNo);
        if (position != null) {
            row.setCode(position.getCode());
            row.setName(position.getName());
            row.setVersion(position.getVersion());
            row.setEffectiveDate(formatDate(position.getEffectiveDate()));
            row.setRevocationDate(formatDate(position.getRevocationDate()));
            row.setOrgUnit(position.getOrgUnit());
            row.setOrgUnitValue(position.getOrgUnitValue());
            row.setJobCategoryValue(StringUtils.defaultIfBlank(position.getJobCategoryValue(), position.getJobCategory()));
            row.setJobGroupValue(StringUtils.defaultIfBlank(position.getJobGroupValue(), position.getJobGroup()));
            row.setJobTypeValue(StringUtils.defaultIfBlank(position.getJobTypeValue(), position.getJobType()));
            row.setJobLevel(position.getJobLevel());
            row.setJobGrade(position.getJobGrade());
            row.setHeadcountQuota(position.getHeadcountQuota());
            row.setControlMode(position.getControlMode());
            row.setIncumbentCount(position.getIncumbentCount());
            row.setIncumbentPersons(position.getIncumbentPersons());
            row.setWorkPlace(position.getWorkPlace());
            row.setWorkContent(position.getWorkContent());
            row.setPositionStatus(positionStatusLabel(position.getPositionStatus()));
            row.setPositionDescStatus(descriptionStatusLabel(position.getPositionDescStatus()));
        }
        if (detail == null || detail.getDescription() == null) {
            return row;
        }
        com.crrc.entity.PositionDescription description = detail.getDescription();
        row.setDescriptionId(description.getId());
        row.setDescriptionVersion(description.getVersion());
        row.setDescriptionStatus(descriptionStatusLabel(description.getStatus()));
        row.setDescriptionEffectiveDate(formatDate(description.getEffectiveDate()));
        row.setDescriptionRevocationDate(formatDate(description.getRevocationDate()));
        row.setDirectSuperiorPositionCode(description.getDirectSuperiorPositionCode());
        row.setDirectSuperiorPositionName(description.getDirectSuperiorPositionName());
        row.setDirectSubordinatePositionCodes(description.getDirectSubordinatePositionCodes());
        row.setDirectSubordinatePositionNames(description.getDirectSubordinatePositionNames());
        row.setPositionPurpose(description.getPositionPurpose());
        row.setWorkEnvironment(description.getWorkEnvironment());
        row.setQualification(description.getQualification());
        row.setRequiredKnowledge(description.getRequiredKnowledge());
        row.setResponsibilities(joinResponsibilities(detail.getResponsibilities()));
        row.setBasisList(joinBasis(detail.getBasisList()));
        row.setCoordinationList(joinCoordinations(detail.getCoordinationList()));
        row.setAuthorityList(joinAuthorities(detail.getAuthorityList()));
        row.setAssessmentList(joinAssessments(detail.getAssessmentList()));
        row.setCompetencyList(joinCompetencies(detail.getCompetencyList()));
        row.setAttachmentFileIds(joinAttachmentFileIds(detail.getAttachments()));
        row.setAttachmentNames(joinAttachmentNames(detail.getAttachments()));
        return row;
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMATTER_1.format(date);
    }

    private String joinResponsibilities(List<com.crrc.entity.PositionDescriptionResponsibility> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> joinParts(item.getResponsibilityName(), item.getResponsibilityDesc()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinBasis(List<com.crrc.entity.PositionDescriptionBasis> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(com.crrc.entity.PositionDescriptionBasis::getBasisContent)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinCoordinations(List<com.crrc.entity.PositionDescriptionCoordination> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> joinParts(item.getCoordinationType(), item.getCoordinationObject(), item.getCoordinationContent()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinAuthorities(List<com.crrc.entity.PositionDescriptionAuthority> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(com.crrc.entity.PositionDescriptionAuthority::getAuthorityContent)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinAssessments(List<com.crrc.entity.PositionDescriptionAssessment> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(com.crrc.entity.PositionDescriptionAssessment::getAssessmentContent)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinCompetencies(List<com.crrc.entity.PositionDescriptionCompetency> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> joinParts(item.getCompetencyCategory(), item.getCompetencyName(), item.getCompetencyLevel()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinAttachmentFileIds(List<com.crrc.entity.PositionDescriptionAttachment> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(com.crrc.entity.PositionDescriptionAttachment::getFileId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String joinAttachmentNames(List<com.crrc.entity.PositionDescriptionAttachment> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> joinParts(item.getFileName(), item.getFileId()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private String joinParts(String... parts) {
        if (parts == null || parts.length == 0) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                values.add(StringUtils.trim(part));
            }
        }
        return values.isEmpty() ? null : String.join(" - ", values);
    }

    private String positionStatusLabel(String status) {
        if (StringUtils.isBlank(status)) {
            return status;
        }
        if (PositionManagementConstants.POSITION_STATUS_ENABLED.equals(status)) {
            return "启用";
        }
        if (PositionManagementConstants.POSITION_STATUS_DISABLED.equals(status)) {
            return "停用";
        }
        return status;
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

    private PositionInfoImportResultVO importFullRows(List<PositionInfoFullImportVO> rows) {
        PositionInfoImportResultVO result = new PositionInfoImportResultVO();
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("导入文件没有数据行");
        }

        List<PositionInfoFullImportVO> validRows = new ArrayList<>();
        Set<String> excelCodes = new LinkedHashSet<>();
        int physicalRowNo = 1;
        for (PositionInfoFullImportVO row : rows) {
            physicalRowNo++;
            if (isEmptyRow(row)) {
                continue;
            }
            result.setTotalRows(result.getTotalRows() + 1);
            normalizeImportRow(row);
            validateImportRow(row, physicalRowNo, result.getErrors());
            if (StringUtils.isNotBlank(row.getCode()) && !excelCodes.add(row.getCode())) {
                result.getErrors().add("第 " + physicalRowNo + " 行：职位编码在Excel中重复：" + row.getCode());
            }
            if (result.getErrors().size() >= MAX_IMPORT_ERROR_COUNT) {
                throw new IllegalArgumentException("导入校验失败，错误过多：\n" + String.join("\n", result.getErrors()));
            }
            validRows.add(row);
        }

        if (result.getTotalRows() == 0) {
            throw new IllegalArgumentException("导入文件没有有效数据行");
        }
        if (result.hasError()) {
            throw new IllegalArgumentException("导入校验失败：\n" + String.join("\n", result.getErrors()));
        }

        List<String> codes = validRows.stream().map(PositionInfoFullImportVO::getCode).collect(Collectors.toList());
        Map<String, PositionInfo> existingMap = selectByCodes(codes).stream()
                .filter(item -> StringUtils.isNotBlank(item.getCode()))
                .collect(Collectors.toMap(PositionInfo::getCode, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<PositionInfo> saveList = new ArrayList<>();
        for (PositionInfoFullImportVO row : validRows) {
            PositionInfo target = existingMap.get(row.getCode());
            boolean insert = target == null;
            if (insert) {
                target = new PositionInfo();
                target.setCode(row.getCode());
                target.setPositionStatus(PositionManagementConstants.POSITION_STATUS_ENABLED);
                target.setPositionDescStatus(PositionManagementConstants.DESC_STATUS_NONE);
                target.setIncumbentCount(0);
                result.setInsertCount(result.getInsertCount() + 1);
            } else {
                result.setUpdateCount(result.getUpdateCount() + 1);
            }
            applyImportRow(row, target, insert);
            normalizePosition(target, false);
            saveList.add(target);
        }
        saveOrUpdateBatch(saveList);
        refreshIncumbents(codes);
        return result;
    }

    private void validateImportRow(PositionInfoFullImportVO row, int rowNo, List<String> errors) {
        if (StringUtils.isBlank(row.getCode())) {
            errors.add("第 " + rowNo + " 行：职位编码不能为空");
        }
        if (StringUtils.isBlank(row.getName())) {
            errors.add("第 " + rowNo + " 行：职位名称不能为空");
        }
        if (StringUtils.isBlank(row.getOrgUnit())) {
            errors.add("第 " + rowNo + " 行：所属组织单元不能为空");
        }
        if (StringUtils.isBlank(row.getJobType())) {
            errors.add("第 " + rowNo + " 行：职种不能为空");
        }
        tryParseLocalDate(row.getEffectiveDate(), rowNo, "生效日期", errors);
        tryParseLocalDate(row.getRevocationDate(), rowNo, "撤销日期", errors);
        tryParseInteger(row.getHeadcountQuota(), rowNo, "编制人数", errors);
    }

    private void applyImportRow(PositionInfoFullImportVO row, PositionInfo target, boolean insert) {
        applyText(row.getCode(), target::setCode);
        applyText(row.getName(), target::setName);
        applyText(row.getVersion(), target::setVersion);
        applyDate(row.getEffectiveDate(), target::setEffectiveDate);
        applyDate(row.getRevocationDate(), target::setRevocationDate);
        applyText(row.getOrgUnit(), target::setOrgUnit);
        applyText(row.getWorkCenter(), target::setWorkCenter);
        applyText(row.getJobCategory(), target::setJobCategory);
        applyText(row.getJobGroup(), target::setJobGroup);
        applyText(row.getJobType(), target::setJobType);
        applyText(row.getJobLevel(), target::setJobLevel);
        applyText(row.getJobGrade(), target::setJobGrade);
        applyInteger(row.getHeadcountQuota(), target::setHeadcountQuota);
        applyText(row.getControlMode(), target::setControlMode);
        applyText(row.getDeptCode(), target::setDeptCode);
        applyText(row.getDeptName(), target::setDeptName);
        applyText(row.getParentPositionCode(), target::setParentPositionCode);
        applyText(row.getParentPositionName(), target::setParentPositionName);
        applyText(row.getRelatedPost(), target::setRelatedPost);
        applyText(row.getWorkPlace(), target::setWorkPlace);
        applyText(row.getWorkContent(), target::setWorkContent);
        if (StringUtils.isNotBlank(row.getPositionStatus())) {
            target.setPositionStatus(normalizePositionStatus(row.getPositionStatus()));
        } else if (insert && StringUtils.isBlank(target.getPositionStatus())) {
            target.setPositionStatus(PositionManagementConstants.POSITION_STATUS_ENABLED);
        }
        if (insert && StringUtils.isBlank(target.getVersion())) {
            target.setVersion("1.0");
        }
    }

    private void normalizePosition(PositionInfo positionInfo, boolean requireCode) {
        if (positionInfo == null) {
            return;
        }
        if (positionInfo.getCode() != null) {
            positionInfo.setCode(StringUtils.trim(positionInfo.getCode()));
        }
        if (requireCode && StringUtils.isBlank(positionInfo.getCode())) {
            throw new IllegalArgumentException("职位编码不能为空");
        }
        if (positionInfo.getName() != null) {
            positionInfo.setName(StringUtils.trim(positionInfo.getName()));
        }
        boolean insert = positionInfo.getId() == null;
        if (StringUtils.isBlank(positionInfo.getPositionStatus())) {
            if (insert) {
                positionInfo.setPositionStatus(PositionManagementConstants.POSITION_STATUS_ENABLED);
            }
        } else {
            positionInfo.setPositionStatus(normalizePositionStatus(positionInfo.getPositionStatus()));
        }
        if (insert && StringUtils.isBlank(positionInfo.getPositionDescStatus())) {
            positionInfo.setPositionDescStatus(PositionManagementConstants.DESC_STATUS_NONE);
        }
        if (insert && positionInfo.getIncumbentCount() == null) {
            positionInfo.setIncumbentCount(0);
        }
    }

    private void validateCodeUnique(String code, Long ignoreId) {
        LambdaQueryWrapper<PositionInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PositionInfo::getCode, code);
        if (ignoreId != null) {
            wrapper.ne(PositionInfo::getId, ignoreId);
        }
        if (exists(wrapper)) {
            throw new IllegalArgumentException("职位编码不能重复：" + code);
        }
    }

    private List<PositionInfo> queryCopySources(PositionInfoCopyRequest request) {
        QueryWrapper<PositionInfo> wrapper = new QueryWrapper<>();
        boolean hasId = request.getIdList() != null && !request.getIdList().isEmpty();
        boolean hasCode = request.getCodeList() != null && !request.getCodeList().isEmpty();
        if (hasId) {
            wrapper.in("id", request.getIdList());
        } else if (hasCode) {
            wrapper.in("code", normalizeCodes(request.getCodeList()));
        } else {
            throw new IllegalArgumentException("源职位ID列表和源职位编码列表不能同时为空");
        }
        List<PositionInfo> list = baseMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getCode()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(PositionInfo::getCode, Function.identity(), (a, b) -> a, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private String resolveCopyCode(String sourceCode, Map<String, String> newCodeMap, Set<String> usedNewCodes) {
        String mappedCode = null;
        if (newCodeMap != null) {
            mappedCode = newCodeMap.get(sourceCode);
        }
        String newCode = StringUtils.trimToNull(mappedCode);
        if (newCode == null) {
            newCode = generateCopyCode(sourceCode, usedNewCodes);
        }
        if (!usedNewCodes.add(newCode)) {
            throw new IllegalArgumentException("复制后的职位编码重复：" + newCode);
        }
        validateCodeUnique(newCode, null);
        return newCode;
    }

    private String generateCopyCode(String sourceCode, Set<String> usedNewCodes) {
        String baseCode = StringUtils.defaultIfBlank(sourceCode, "POSITION") + "_COPY";
        String candidate = baseCode;
        int index = 1;
        while (usedNewCodes.contains(candidate) || exists(new LambdaQueryWrapper<PositionInfo>().eq(PositionInfo::getCode, candidate))) {
            index++;
            candidate = baseCode + index;
        }
        return candidate;
    }

    private void fillIncumbentFields(List<PositionInfo> records) {
        List<String> codes = records.stream()
                .filter(Objects::nonNull)
                .map(PositionInfo::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) {
            return;
        }

        // /positionInfo/query 是历史外部集成接口，空条件时会一次返回上万条职位。
        // 此时不要实时 JOIN hr_emp_appointment/hr_emp_basic 统计在岗人员，直接保留 position_info 表中的快照字段。
        // 如需刷新快照，由管理端调用 /positionInfo/refreshIncumbents 显式刷新。
        if (codes.size() > MAX_REALTIME_INCUMBENT_RESOLVE_COUNT) {
            log.warn("Skip realtime incumbent resolving, position size {} exceeds threshold {}. Use snapshot fields instead.",
                    codes.size(), MAX_REALTIME_INCUMBENT_RESOLVE_COUNT);
            return;
        }

        Map<String, List<PositionIncumbentDTO>> incumbentMap;
        try {
            incumbentMap = queryIncumbentMap(codes);
        } catch (RuntimeException e) {
            // 在岗人员只是展示增强字段，不能因为增强查询异常影响职位主数据查询。
            log.warn("Realtime incumbent resolving failed, fallback to position_info snapshot fields. size={}", codes.size(), e);
            return;
        }
        for (PositionInfo position : records) {
            if (position == null || StringUtils.isBlank(position.getCode())) {
                continue;
            }
            List<PositionIncumbentDTO> incumbents = incumbentMap.getOrDefault(position.getCode(), Collections.emptyList());
            position.setIncumbentCount(incumbents.size());
            position.setIncumbentPersons(joinIncumbentPersons(incumbents));
        }
    }

    private Map<String, List<PositionIncumbentDTO>> queryIncumbentMap(Collection<String> codes) {
        List<String> normalizedCodes = normalizeCodes(codes);
        if (normalizedCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PositionIncumbentDTO> incumbents = new ArrayList<>();
        for (int start = 0; start < normalizedCodes.size(); start += INCUMBENT_QUERY_BATCH_SIZE) {
            int end = Math.min(start + INCUMBENT_QUERY_BATCH_SIZE, normalizedCodes.size());
            List<String> batchCodes = normalizedCodes.subList(start, end);
            List<PositionIncumbentDTO> batch = baseMapper.selectActiveIncumbentsByPositionCodes(batchCodes);
            if (batch != null && !batch.isEmpty()) {
                incumbents.addAll(batch);
            }
        }
        if (incumbents.isEmpty()) {
            return Collections.emptyMap();
        }
        return incumbents.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getPositionCode()))
                .collect(Collectors.groupingBy(PositionIncumbentDTO::getPositionCode, LinkedHashMap::new, Collectors.toList()));
    }

    private String joinIncumbentPersons(List<PositionIncumbentDTO> incumbents) {
        if (incumbents == null || incumbents.isEmpty()) {
            return null;
        }
        return incumbents.stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    String userCode = StringUtils.trimToEmpty(item.getUserCode());
                    String userName = StringUtils.trimToEmpty(item.getUserName());
                    if (StringUtils.isBlank(userCode)) {
                        return userName;
                    }
                    if (StringUtils.isBlank(userName) || userCode.equals(userName)) {
                        return userCode;
                    }
                    return userCode + ":" + userName;
                })
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private List<String> normalizeCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return codes.stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void normalizeImportRow(PositionInfoFullImportVO row) {
        if (row == null) {
            return;
        }
        row.setCode(normalizeImportText(row.getCode()));
        if (StringUtils.startsWith(row.getCode(), "-")) {
            row.setCode(row.getCode().replaceFirst("-", ""));
        }
        row.setName(normalizeImportText(row.getName()));
        row.setVersion(normalizeImportText(row.getVersion()));
        row.setEffectiveDate(normalizeImportText(row.getEffectiveDate()));
        row.setRevocationDate(normalizeImportText(row.getRevocationDate()));
        row.setOrgUnit(normalizeImportText(row.getOrgUnit()));
        row.setWorkCenter(normalizeImportText(row.getWorkCenter()));
        row.setJobCategory(normalizeImportText(row.getJobCategory()));
        row.setJobGroup(normalizeImportText(row.getJobGroup()));
        row.setJobType(normalizeImportText(row.getJobType()));
        row.setJobLevel(normalizeImportText(row.getJobLevel()));
        row.setJobGrade(normalizeImportText(row.getJobGrade()));
        row.setHeadcountQuota(normalizeImportText(row.getHeadcountQuota()));
        row.setControlMode(normalizeImportText(row.getControlMode()));
        row.setDeptCode(normalizeImportText(row.getDeptCode()));
        row.setDeptName(normalizeImportText(row.getDeptName()));
        row.setParentPositionCode(normalizeImportText(row.getParentPositionCode()));
        row.setParentPositionName(normalizeImportText(row.getParentPositionName()));
        row.setRelatedPost(normalizeImportText(row.getRelatedPost()));
        row.setWorkPlace(normalizeImportText(row.getWorkPlace()));
        row.setWorkContent(normalizeImportText(row.getWorkContent()));
        row.setPositionStatus(normalizeImportText(row.getPositionStatus()));
    }

    private String normalizeImportText(String text) {
        return StringUtils.trimToNull(text);
    }

    private boolean isEmptyRow(PositionInfoFullImportVO row) {
        if (row == null) {
            return true;
        }
        return StringUtils.isBlank(row.getCode())
                && StringUtils.isBlank(row.getName())
                && StringUtils.isBlank(row.getOrgUnit())
                && StringUtils.isBlank(row.getJobType())
                && StringUtils.isBlank(row.getJobGrade());
    }

    private void tryParseLocalDate(String value, int rowNo, String fieldName, List<String> errors) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            parseLocalDate(value);
        } catch (Exception e) {
            errors.add("第 " + rowNo + " 行：" + fieldName + "格式不正确，支持 yyyy-MM-dd、yyyy/MM/dd、yyyyMMdd");
        }
    }

    private void tryParseInteger(String value, int rowNo, String fieldName, List<String> errors) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            parseInteger(value);
        } catch (Exception e) {
            errors.add("第 " + rowNo + " 行：" + fieldName + "必须是整数");
        }
    }

    private void applyText(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(StringUtils.trim(value));
        }
    }

    private void applyDate(String value, java.util.function.Consumer<LocalDate> setter) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(parseLocalDate(value));
        }
    }

    private void applyInteger(String value, java.util.function.Consumer<Integer> setter) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(parseInteger(value));
        }
    }

    private LocalDate parseLocalDate(String value) {
        String text = StringUtils.trim(value);
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String numeric = StringUtils.removeEnd(text, ".0");
        if (numeric.matches("^\\d{5}$")) {
            return LocalDate.of(1899, 12, 30).plusDays(Long.parseLong(numeric));
        }
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DATE_FORMATTER_1);
        formatters.add(DATE_FORMATTER_2);
        formatters.add(DATE_FORMATTER_3);
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        throw new IllegalArgumentException("日期格式不正确：" + value);
    }

    private Integer parseInteger(String value) {
        String text = StringUtils.trim(value);
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return Integer.valueOf(StringUtils.removeEnd(text, ".0"));
    }

    private String normalizePositionStatus(String status) {
        String text = StringUtils.trimToEmpty(status);
        if ("启用".equals(text) || "1".equals(text) || PositionManagementConstants.POSITION_STATUS_ENABLED.equalsIgnoreCase(text)) {
            return PositionManagementConstants.POSITION_STATUS_ENABLED;
        }
        if ("停用".equals(text) || "0".equals(text) || PositionManagementConstants.POSITION_STATUS_DISABLED.equalsIgnoreCase(text)) {
            return PositionManagementConstants.POSITION_STATUS_DISABLED;
        }
        return text;
    }
}
