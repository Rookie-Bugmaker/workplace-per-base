package com.crrc.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.crrc.entity.*;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.HrEmpExportType;
import com.crrc.excel.YearMonthStringConverter;
import com.crrc.mapper.*;
import com.crrc.pojo.dto.HrEmpBatchExportRequest;
import com.crrc.pojo.vo.HrEmpBasicExportVO;
import com.crrc.service.CodeInfoService;
import com.crrc.service.ExportProgressUpdater;
import com.crrc.service.HrEmpExportService;
import com.crrc.service.impl.translation.HrEmpExportTranslatorRegistry;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.HrEmpExportMergeStrategy;
import com.crrc.utils.SystemConstants;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 人事信息导出服务实现（从原同步导出逻辑抽取）
 */
@Service
@Slf4j
public class HrEmpExportServiceImpl implements HrEmpExportService {

    /**
     * 不得导出的非业务字段
     */
    private static final Set<String> EXCLUDE_FIELDS = new HashSet<>(Arrays.asList(
            "id", "tenantId", "tenant_id", "deleted", "delFlag", "version",
            "createBy", "createTime", "updateBy", "updateTime",
            "createPerson", "updatePerson", "dynamicFields", "remarks", "remark",
            "serialVersionUID"
    ));

    /**
     * 前两列已手动固定为姓名、档案号，不再从实体字段中提取
     */
    private static final Set<String> FIXED_HEAD_FIELDS = new HashSet<>(Arrays.asList("name", "userCode"));

    /**
     * userCode 分批查询大小。
     * <p>
     * 说明：避免超长 IN SQL 导致 MyBatis-Plus SQL 解析失败或数据库执行失败；
     * 兼容 Oracle 等数据库 IN 条件 1000 个元素限制。
     * </p>
     */
    private static final int USER_CODE_IN_BATCH_SIZE = 500;

    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    private HrEmpEducationMapper hrEmpEducationMapper;
    @Autowired
    private HrEmpCareerPathMapper hrEmpCareerPathMapper;
    @Autowired
    private HrEmpFamMemberMapper hrEmpFamMemberMapper;
    @Autowired
    private HrEmpProfessionalQualificationMapper hrEmpProQualificationMapper;
    @Autowired
    private HrEmpSkillLevelMapper hrEmpSkillLevelMapper;
    @Autowired
    private HrEmpSalaryInfoMapper hrEmpSalaryInfoMapper;
    @Autowired
    private HrEmpSalaryStandardMapper hrEmpSalaryStandardMapper;
    @Autowired
    private HrEmpPenaltiesMapper hrEmpPenaltiesMapper;
    @Autowired
    private HrEmpPatentMapper hrEmpPatentMapper;
    @Autowired
    private HrEmpPaperMapper hrEmpPaperMapper;
    @Autowired
    private HrEmpAppointmentMapper hrEmpAppointmentMapper;
    @Autowired
    private HrEmpOtherAppointmentMapper hrEmpOtherAppointmentMapper;
    @Autowired
    private HrEmpForeignLanguageMapper hrEmpForeignLanguageMapper;
    @Autowired
    private HrEmpHonorMapper hrEmpHonorMapper;
    @Autowired
    private HrEmpResumeMapper hrEmpResumeMapper;
    @Autowired
    private HrEmpRewardsMapper hrEmpRewardsMapper;
    @Autowired
    private HrEmpCertificateMapper hrEmpCertificateMapper;
    @Autowired
    private HrEmpHouseholdMapper hrEmpHouseholdMapper;
    @Autowired
    private HrEmpCommunicationMapper hrEmpCommunicationMapper;
    @Autowired
    private HrEmpHealthMapper hrEmpHealthMapper;
    @Autowired
    private CodeInfoService codeInfoService;
    @Autowired
    private HrEmpExportTranslatorRegistry translatorRegistry;
    @Autowired
    private HrEmpExportTaskServiceImpl hrEmpExportTaskService;

    @Override
    public byte[] exportHrEmpInfo(HrEmpBatchExportRequest request,
                                  ExportProgressUpdater progressUpdater) {

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.QUERY_START_PROGRESS);
        }

        // 1. 查询人员基本信息
        List<HrEmpBasicExportVO> basicList = queryBasicList(request);

        if (progressUpdater != null) {
            progressUpdater.updateProgress(10);
        }

        List<String> userCodes = basicList.stream()
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 确定要导出的信息集
        List<HrEmpExportType> exportTypes = resolveExportTypes(request.getExportTypes());

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.EXPORT_PRERESOLVE_END_PROGRESS);
        }

        // 3. 预加载翻译数据
        Set<Class<?>> entityClasses = exportTypes.stream()
                .map(HrEmpExportType::getEntityClass)
                .collect(Collectors.toSet());

        translatorRegistry.prefetchAll(entityClasses, codeInfoService);

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.TRANSLATE_END_PROGRESS);
        }

        // 4. 子表数据只查询一次，并缓存。
        // 基本信息中的“职位”依赖任职信息；将依赖查询纳入同一查询计划，
        // 复用已选择导出的任职数据，并统一计入异步任务查询进度。
        Map<HrEmpExportType, List<?>> subDataMap = new EnumMap<>(HrEmpExportType.class);
        Map<HrEmpExportType, List<String>> subTableQueryPlan = buildSubTableQueryPlan(
                exportTypes,
                basicList,
                userCodes
        );

        int totalQueryBatchCount = subTableQueryPlan.values().stream()
                .mapToInt(this::countUserCodeBatches)
                .sum();

        AtomicInteger finishedQueryBatchCount = new AtomicInteger(0);
        AtomicInteger lastQueryProgress = new AtomicInteger(0);

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.TRANSLATE_END_PROGRESS);
        }

        for (Map.Entry<HrEmpExportType, List<String>> queryEntry : subTableQueryPlan.entrySet()) {
            HrEmpExportType exportType = queryEntry.getKey();
            List<?> subData = querySubTable(exportType, queryEntry.getValue(), () -> {
                if (progressUpdater == null) {
                    return;
                }

                int finished = finishedQueryBatchCount.incrementAndGet();

                int progress = hrEmpExportTaskService.calculateStageProgress(
                        HrEmpExportTaskServiceImpl.TRANSLATE_END_PROGRESS,
                        HrEmpExportTaskServiceImpl.QUERY_END_PROGRESS,
                        finished,
                        totalQueryBatchCount
                );

                // 避免同一个百分比重复 update 数据库
                int lastProgress = lastQueryProgress.get();
                if (progress > lastProgress && lastQueryProgress.compareAndSet(lastProgress, progress)) {
                    progressUpdater.updateProgress(progress);
                }
            });

            subDataMap.put(exportType, subData);
        }

        if (exportTypes.contains(HrEmpExportType.BASIC)) {
            fillBasicPosition(
                    basicList,
                    getCachedSubData(subDataMap, HrEmpExportType.APPOINTMENT),
                    getCachedSubData(subDataMap, HrEmpExportType.OTHER_APPOINTMENT)
            );
        }

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.QUERY_END_PROGRESS);
        }

        // 5. 计算真实写入行数
        int totalCount = calculateExportTotalCount(exportTypes, basicList, subDataMap);

        if (progressUpdater != null) {
            progressUpdater.initTotal(totalCount);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ExcelWriter excelWriter = EasyExcelFactory.write(baos)
                .registerConverter(new YearMonthStringConverter())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(ExcelTypeEnum.XLSX)
                .build();

        try {
            for (HrEmpExportType exportType : exportTypes) {
                if (exportType == HrEmpExportType.BASIC) {
                    writeBasicSheet(excelWriter, basicList);

                    if (progressUpdater != null) {
                        progressUpdater.increment(basicList.size());
                    }
                } else {
                    List<?> subData = subDataMap.getOrDefault(exportType, Collections.emptyList());

                    writeSubTableSheetWithProgress(
                            excelWriter,
                            exportType,
                            basicList,
                            userCodes,
                            subData,
                            progressUpdater
                    );
                }
            }
        } finally {
            excelWriter.finish();
        }

        if (progressUpdater != null) {
            progressUpdater.updateProgress(HrEmpExportTaskServiceImpl.FINISH_EXCEL_PROGRESS);
        }

        return baos.toByteArray();
    }

    /**
     * 构建子表查询计划。
     * <p>
     * 当导出基本信息时，职位字段依赖任职信息：
     * 已选择导出任职 Sheet 时直接复用其全量查询；未选择时仅按人员分类查询职位所需的数据。
     * </p>
     */
    private Map<HrEmpExportType, List<String>> buildSubTableQueryPlan(
            List<HrEmpExportType> exportTypes,
            List<? extends HrEmpBasic> basicList,
            List<String> userCodes) {

        Map<HrEmpExportType, List<String>> queryPlan = new LinkedHashMap<>();

        for (HrEmpExportType exportType : exportTypes) {
            if (exportType != HrEmpExportType.BASIC) {
                queryPlan.put(exportType, userCodes);
            }
        }

        if (!exportTypes.contains(HrEmpExportType.BASIC)) {
            return queryPlan;
        }

        List<String> appointmentCodes = basicList.stream()
                .filter(Objects::nonNull)
                .filter(basic -> !"002".equals(basic.getCategory8()))
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> otherAppointmentCodes = basicList.stream()
                .filter(Objects::nonNull)
                .filter(basic -> "002".equals(basic.getCategory8()))
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!appointmentCodes.isEmpty()) {
            queryPlan.putIfAbsent(HrEmpExportType.APPOINTMENT, appointmentCodes);
        }
        if (!otherAppointmentCodes.isEmpty()) {
            queryPlan.putIfAbsent(HrEmpExportType.OTHER_APPOINTMENT, otherAppointmentCodes);
        }

        return queryPlan;
    }

    /**
     * 从已缓存的任职数据中抽取每名员工的最新职位并写回基本信息导出 VO。
     */
    private void fillBasicPosition(List<HrEmpBasicExportVO> basicList,
                                   List<HrEmpAppointment> appointmentList,
                                   List<HrEmpOtherAppointment> otherAppointmentList) {
        if (basicList == null || basicList.isEmpty()) {
            return;
        }

        Set<String> otherAppointmentCodes = basicList.stream()
                .filter(Objects::nonNull)
                .filter(basic -> "002".equals(basic.getCategory8()))
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> appointmentCodes = basicList.stream()
                .filter(Objects::nonNull)
                .filter(basic -> !"002".equals(basic.getCategory8()))
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, HrEmpAppointment> latestAppointmentMap = new HashMap<>();

        if (otherAppointmentList != null && !otherAppointmentList.isEmpty()) {
            Map<String, HrEmpAppointment> latestOtherAppointmentMap = otherAppointmentList.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.getUserCode() != null)
                    .filter(item -> otherAppointmentCodes.contains(item.getUserCode()))
                    .filter(item -> item.getStartDate() != null)
                    .map(this::convertToHrEmpAppointment)
                    .collect(Collectors.toMap(
                            HrEmpAppointment::getUserCode,
                            Function.identity(),
                            this::pickLatestAppointment
                    ));
            latestAppointmentMap.putAll(latestOtherAppointmentMap);
        }

        if (appointmentList != null && !appointmentList.isEmpty()) {
            Map<String, HrEmpAppointment> latestNormalAppointmentMap = appointmentList.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.getUserCode() != null)
                    .filter(item -> appointmentCodes.contains(item.getUserCode()))
                    .filter(item -> item.getStartDate() != null)
                    .collect(Collectors.toMap(
                            HrEmpAppointment::getUserCode,
                            Function.identity(),
                            this::pickLatestAppointment
                    ));
            latestAppointmentMap.putAll(latestNormalAppointmentMap);
        }

        for (HrEmpBasicExportVO basic : basicList) {
            HrEmpAppointment appointment = latestAppointmentMap.get(basic.getUserCode());
            if (appointment != null) {
                basic.setPosition(appointment.getPosition());
            }
        }
    }

    /**
     * 获取查询缓存中的指定类型数据。
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getCachedSubData(Map<HrEmpExportType, List<?>> subDataMap,
                                         HrEmpExportType exportType) {
        return (List<T>) subDataMap.getOrDefault(exportType, Collections.emptyList());
    }

    /**
     * 将 HrEmpOtherAppointment 转为 HrEmpAppointment。
     */
    private HrEmpAppointment convertToHrEmpAppointment(HrEmpOtherAppointment source) {
        HrEmpAppointment target = new HrEmpAppointment();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 比较两条任职记录，返回“更新”的那一条。
     * 规则：先比较 startDate，startDate 相同再比较 createTime。
     */
    private HrEmpAppointment pickLatestAppointment(HrEmpAppointment a, HrEmpAppointment b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }

        int startDateCompare = Comparator.<LocalDate>nullsLast(Comparator.naturalOrder())
                .compare(a.getStartDate(), b.getStartDate());
        if (startDateCompare != 0) {
            return startDateCompare >= 0 ? a : b;
        }

        int createTimeCompare = Comparator.<LocalDateTime>nullsLast(Comparator.naturalOrder())
                .compare(a.getCreateTime(), b.getCreateTime());
        return createTimeCompare >= 0 ? a : b;
    }

    private int countUserCodeBatches(Collection<String> userCodes) {
        if (userCodes == null || userCodes.isEmpty()) {
            return 0;
        }

        long distinctCount = userCodes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .count();

        if (distinctCount == 0) {
            return 0;
        }

        return (int) ((distinctCount + USER_CODE_IN_BATCH_SIZE - 1) / USER_CODE_IN_BATCH_SIZE);
    }

    private int calculateExportTotalCount(List<HrEmpExportType> exportTypes,
                                          List<? extends HrEmpBasic> basicList,
                                          Map<HrEmpExportType, List<?>> subDataMap) {
        int totalCount = 0;

        for (HrEmpExportType exportType : exportTypes) {
            if (exportType == HrEmpExportType.BASIC) {
                totalCount += basicList.size();
                continue;
            }

            List<?> subData = subDataMap.getOrDefault(exportType, Collections.emptyList());

            Map<String, Integer> subCountMap = new HashMap<>();
            for (Object item : subData) {
                String uc = getUserCode(item);
                if (uc != null) {
                    subCountMap.merge(uc, 1, Integer::sum);
                }
            }

            for (HrEmpBasic basic : basicList) {
                String userCode = basic.getUserCode();
                if (userCode != null) {
                    totalCount += subCountMap.getOrDefault(userCode, 0);
                }
            }
        }

        return totalCount;
    }

    /**
     * 查询符合条件的人员基本信息
     */
    private List<HrEmpBasicExportVO> queryBasicList(HrEmpBatchExportRequest request) {
        MPJLambdaWrapper<HrEmpBasic> wrapper = new MPJLambdaWrapper<>();

        wrapper.selectAll(HrEmpBasic.class);
        wrapper.like(StringUtils.isNotBlank(request.getName()), HrEmpBasic::getName, nvl(request.getName()).trim());
        wrapper.like(StringUtils.isNotBlank(request.getUserCode()), HrEmpBasic::getUserCode, nvl(request.getUserCode()).trim());
        // 如果穿了isEmployed，则根据传入的是否在职查询
        wrapper.eq(StringUtils.isNotBlank(request.getIsEmployed()), HrEmpBasic::getIsEmployed, request.getIsEmployed());

        // 如果传了isIntern且为1，那么查询实习生，否则排除实习生
        if(StringUtils.isNotBlank(request.getIsIntern()) && SystemConstants.TRUE_FLAG.equals(request.getIsIntern())) {
            wrapper.eq(HrEmpBasic::getCategory1, "003");
        } else {
            wrapper.ne(HrEmpBasic::getCategory1, "003");
        }

        if (StringUtils.isNotEmpty(request.getDeptCode())) {
            Set<String> deptCodes = DeptUtils.childrenOrganization(request.getDeptCode(), true)
                    .stream()
                    .map(OrganizationTreeDTO::getOrgCode)
                    .collect(Collectors.toSet());

            wrapper.in(HrEmpBasic::getDept, deptCodes);
        }

        if (ObjectUtils.isNotEmpty(request.getRequireDept())) {
            wrapper.in(HrEmpBasic::getDept, request.getRequireDept());
        }

        List<HrEmpBasic> basicList = hrEmpBasicMapper.selectList(wrapper);
        if (basicList.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> userCodes = basicList.stream()
                .map(HrEmpBasic::getUserCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 分批查询 4 张关联表数据
        Map<String, HrEmpCertificate> certMap = toSingleRecordMap(
                selectListByUserCodesInBatches(hrEmpCertificateMapper, HrEmpCertificate::getUserCode, userCodes, null),
                HrEmpCertificate::getUserCode);

        Map<String, HrEmpHouseHold> householdMap = toSingleRecordMap(
                selectListByUserCodesInBatches(hrEmpHouseholdMapper, HrEmpHouseHold::getUserCode, userCodes, null),
                HrEmpHouseHold::getUserCode);

        Map<String, HrEmpCommunication> commMap = toSingleRecordMap(
                selectListByUserCodesInBatches(hrEmpCommunicationMapper, HrEmpCommunication::getUserCode, userCodes, null),
                HrEmpCommunication::getUserCode);

        Map<String, HrEmpHealth> healthMap = toSingleRecordMap(
                selectListByUserCodesInBatches(hrEmpHealthMapper, HrEmpHealth::getUserCode, userCodes, null),
                HrEmpHealth::getUserCode);

        // 合并为 VO 对象
        List<HrEmpBasicExportVO> voList = new ArrayList<>(basicList.size());
        for (HrEmpBasic basic : basicList) {
            HrEmpBasicExportVO vo = new HrEmpBasicExportVO();
            BeanUtils.copyProperties(basic, vo);

            String uc = basic.getUserCode();

            HrEmpCertificate cert = certMap.get(uc);
            if (cert != null) {
                vo.setCertiType(cert.getCertiType());
                vo.setCertiId(cert.getCertiId());
            }

            HrEmpHouseHold hh = householdMap.get(uc);
            if (hh != null) {
                vo.setHouseHoldType(hh.getHouseHoldType());
                vo.setHouseHoldLocation(hh.getHouseHoldLocation());
                vo.setHouseHoldLocationDetail(hh.getHouseHoldLocationDetail());
            }

            HrEmpCommunication comm = commMap.get(uc);
            if (comm != null) {
                vo.setMobilePhone(comm.getMobilePhone());
                vo.setWorkEmail(comm.getWorkEmail());
                vo.setPersonalEmail(comm.getPersonalEmail());
                vo.setAddress(comm.getAddress());
            }

            HrEmpHealth health = healthMap.get(uc);
            if (health != null) {
                vo.setHealthStatus(health.getHealthStatus());
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 将实体列表转为以 userCode 为 key 的 Map（一对一时保留第一条）
     */
    private <T> Map<String, T> toSingleRecordMap(List<T> list,
                                                  java.util.function.Function<T, String> keyExtractor) {
        return list.stream()
                .collect(Collectors.toMap(keyExtractor, t -> t, (a, b) -> a));
    }

    private String nvl(String str) {
        return str == null ? "" : str;
    }

    /**
     * 确定要导出的信息集列表
     */
    private List<HrEmpExportType> resolveExportTypes(List<String> exportTypes) {
        if (exportTypes == null || exportTypes.isEmpty()) {
            return Arrays.asList(HrEmpExportType.values());
        }
        List<HrEmpExportType> result = new ArrayList<>();
        for (String code : exportTypes) {
            HrEmpExportType type = HrEmpExportType.fromCode(code);
            if (type != null) {
                result.add(type);
            } else {
                log.warn("未知的导出类型: {}", code);
            }
        }
        return result;
    }

    // ==================== 基本信息 Sheet ====================

    /**
     * 写入基本信息 Sheet
     */
    private void writeBasicSheet(ExcelWriter excelWriter, List<HrEmpBasicExportVO> basicList) {
        Class<?> entityClass = HrEmpBasicExportVO.class;
        List<ExportFieldMeta> fieldMetas = extractFieldMetas(entityClass);

        // 前两列固定为姓名、档案号
        List<List<String>> head = buildBasicHead();
        for (ExportFieldMeta meta : fieldMetas) {
            head.add(Collections.singletonList(meta.getHeaderName()));
        }

        // 构建数据
        List<List<Object>> data = new ArrayList<>();
        for (HrEmpBasic basic : basicList) {
            List<Object> row = new ArrayList<>();
            row.add(basic.getName());
            row.add(basic.getUserCode());
            for (ExportFieldMeta meta : fieldMetas) {
                Object rawValue = getFieldValue(basic, meta.getField());
                Object exportValue = translatorRegistry.translate(entityClass, meta.getField(), rawValue, basic);
                row.add(exportValue);
            }
            data.add(row);
        }

        WriteSheet writeSheet = EasyExcelFactory.writerSheet("基本信息").head(head).build();
        excelWriter.write(data, writeSheet);
    }

    // ==================== 子表 Sheet ====================

    /**
     * 写入子表 Sheet（带进度更新）
     */
    private void writeSubTableSheetWithProgress(ExcelWriter excelWriter,
                                                HrEmpExportType exportType,
                                                List<? extends HrEmpBasic> basicList,
                                                List<String> userCodes,
                                                List<?> subData,
                                                ExportProgressUpdater progressUpdater) {
        Class<?> entityClass = exportType.getEntityClass();
        List<ExportFieldMeta> fieldMetas = extractFieldMetas(entityClass);

        Map<String, List<Object>> subMap = new LinkedHashMap<>();
        for (String uc : userCodes) {
            subMap.put(uc, new ArrayList<>());
        }

        for (Object item : subData) {
            String uc = getUserCode(item);
            if (uc != null && subMap.containsKey(uc)) {
                subMap.get(uc).add(item);
            }
        }

        List<List<String>> head = buildBasicHead();
        for (ExportFieldMeta meta : fieldMetas) {
            head.add(Collections.singletonList(meta.getHeaderName()));
        }

        // 先计算行对应的 userCode，用于合并策略
        List<String> rowUserCodes = new ArrayList<>();
        for (HrEmpBasic basic : basicList) {
            List<Object> subItems = subMap.getOrDefault(
                    basic.getUserCode(),
                    Collections.emptyList()
            );

            for (int i = 0; i < subItems.size(); i++) {
                rowUserCodes.add(basic.getUserCode());
            }
        }

        HrEmpExportMergeStrategy mergeStrategy = new HrEmpExportMergeStrategy(rowUserCodes);

        WriteSheet writeSheet = EasyExcelFactory.writerSheet(exportType.getSheetName())
                .head(head)
                .registerWriteHandler(mergeStrategy)
                .build();

        final int batchSize = 100;
        List<List<Object>> batchData = new ArrayList<>(batchSize);

        for (HrEmpBasic basic : basicList) {
            List<Object> subItems = subMap.getOrDefault(
                    basic.getUserCode(),
                    Collections.emptyList()
            );

            for (Object subItem : subItems) {
                List<Object> row = new ArrayList<>();

                row.add(basic.getName());
                row.add(basic.getUserCode());

                for (ExportFieldMeta meta : fieldMetas) {
                    Object rawValue = getFieldValue(subItem, meta.getField());
                    Object exportValue = translatorRegistry.translate(
                            entityClass,
                            meta.getField(),
                            rawValue,
                            subItem
                    );
                    row.add(exportValue);
                }

                batchData.add(row);

                if (batchData.size() >= batchSize) {
                    excelWriter.write(batchData, writeSheet);

                    if (progressUpdater != null) {
                        progressUpdater.increment(batchData.size());
                    }

                    batchData.clear();
                }
            }
        }

        if (!batchData.isEmpty()) {
            excelWriter.write(batchData, writeSheet);

            if (progressUpdater != null) {
                progressUpdater.increment(batchData.size());
            }
        }

        // 没有数据时也写出空 Sheet 表头
        if (rowUserCodes.isEmpty()) {
            excelWriter.write(Collections.emptyList(), writeSheet);
        }
    }

    /**
     * 查询子表数据
     */
    private List<?> querySubTable(HrEmpExportType exportType,
                                  List<String> userCodes,
                                  Runnable afterBatchQueryCompleted) {
        if (userCodes == null || userCodes.isEmpty()) {
            return Collections.emptyList();
        }

        Class<?> entityClass = exportType.getEntityClass();

        if (entityClass == HrEmpEducation.class) {
            return selectListByUserCodesInBatches(
                    hrEmpEducationMapper,
                    HrEmpEducation::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpCareerPath.class) {
            return selectListByUserCodesInBatches(
                    hrEmpCareerPathMapper,
                    HrEmpCareerPath::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpFamMember.class) {
            return selectListByUserCodesInBatches(
                    hrEmpFamMemberMapper,
                    HrEmpFamMember::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpProfessionalQualification.class) {
            return selectListByUserCodesInBatches(
                    hrEmpProQualificationMapper,
                    HrEmpProfessionalQualification::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpSkillLevel.class) {
            return selectListByUserCodesInBatches(
                    hrEmpSkillLevelMapper,
                    HrEmpSkillLevel::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpSalaryInfo.class) {
            return selectListByUserCodesInBatches(
                    hrEmpSalaryInfoMapper,
                    HrEmpSalaryInfo::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpSalaryStandard.class) {
            return selectListByUserCodesInBatches(
                    hrEmpSalaryStandardMapper,
                    HrEmpSalaryStandard::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpPenalties.class) {
            return selectListByUserCodesInBatches(
                    hrEmpPenaltiesMapper,
                    HrEmpPenalties::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpPatent.class) {
            return selectListByUserCodesInBatches(
                    hrEmpPatentMapper,
                    HrEmpPatent::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpPaper.class) {
            return selectListByUserCodesInBatches(
                    hrEmpPaperMapper,
                    HrEmpPaper::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpAppointment.class) {
            return selectListByUserCodesInBatches(
                    hrEmpAppointmentMapper,
                    HrEmpAppointment::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpOtherAppointment.class) {
            return selectListByUserCodesInBatches(
                    hrEmpOtherAppointmentMapper,
                    HrEmpOtherAppointment::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpForeignLanguage.class) {
            return selectListByUserCodesInBatches(
                    hrEmpForeignLanguageMapper,
                    HrEmpForeignLanguage::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        }else if (entityClass == HrEmpHonor.class) {
            return selectListByUserCodesInBatches(
                    hrEmpHonorMapper,
                    HrEmpHonor::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpResume.class) {
            return selectListByUserCodesInBatches(
                    hrEmpResumeMapper,
                    HrEmpResume::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        } else if (entityClass == HrEmpRewards.class) {
            return selectListByUserCodesInBatches(
                    hrEmpRewardsMapper,
                    HrEmpRewards::getUserCode,
                    userCodes,
                    afterBatchQueryCompleted
            );
        }

        return Collections.emptyList();
    }

    /**
     * 按 userCode 分批查询子表数据，避免 userCodes 过大导致 IN SQL 过长。
     *
     * @param mapper         MyBatis-Plus BaseMapper
     * @param userCodeColumn userCode 字段 Lambda 引用
     * @param userCodes      待查询的 userCode 集合
     * @param <T>            实体类型
     * @param afterBatchQueryCompleted 批次完成回调
     * @return 查询结果列表
     */
    private <T> List<T> selectListByUserCodesInBatches(BaseMapper<T> mapper,
                                                       SFunction<T, ?> userCodeColumn,
                                                       Collection<String> userCodes,
                                                       Runnable afterBatchQueryCompleted) {
        if (userCodes == null || userCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> distinctUserCodes = userCodes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (distinctUserCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();

        for (int fromIndex = 0; fromIndex < distinctUserCodes.size(); fromIndex += USER_CODE_IN_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + USER_CODE_IN_BATCH_SIZE, distinctUserCodes.size());
            List<String> batchUserCodes = distinctUserCodes.subList(fromIndex, toIndex);

            LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
            wrapper.in(userCodeColumn, batchUserCodes);

            List<T> batchResult = mapper.selectList(wrapper);
            if (batchResult != null && !batchResult.isEmpty()) {
                result.addAll(batchResult);
            }

            // 关键：一批 SQL 查询完成后，推进查询进度
            if (afterBatchQueryCompleted != null) {
                afterBatchQueryCompleted.run();
            }
        }

        return result;
    }

    /**
     * 获取对象的 userCode 字段值
     */
    private String getUserCode(Object obj) {
        try {
            Field f = findField(obj.getClass(), "userCode");
            if (f == null) {
                return null;
            }
            f.setAccessible(true);
            Object val = f.get(obj);
            return val != null ? val.toString() : null;
        } catch (IllegalAccessException e) {
            log.warn("获取 userCode 失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 字段元数据提取 ====================

    /**
     * 从实体类提取可导出的字段元数据（遍历类继承链）
     */
    private List<ExportFieldMeta> extractFieldMetas(Class<?> entityClass) {
        List<ExportFieldMeta> metas = new ArrayList<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();

            for (Field field : fields) {
                if (EXCLUDE_FIELDS.contains(field.getName())) {
                    continue;
                }
                if (FIXED_HEAD_FIELDS.contains(field.getName())) {
                    continue;
                }
                ExcelProperty ep = field.getAnnotation(ExcelProperty.class);
                if (ep == null) {
                    continue;
                }
                String headerName = ep.value().length > 0 ? ep.value()[0] : field.getName();
                int sortIndex = ep.index();
                metas.add(new ExportFieldMeta(field.getName(), headerName, sortIndex));
            }
            current = current.getSuperclass();
        }

        metas.sort(Comparator.comparingInt(ExportFieldMeta::getSortIndex));
        return metas;
    }

    /**
     * 构建前两列固定表头：姓名、档案号
     */
    private List<List<String>> buildBasicHead() {
        return new ArrayList<>(Arrays.asList(
                Collections.singletonList("姓名"),
                Collections.singletonList("档案号")
        ));
    }

    /**
     * 通过反射获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field f = findField(obj.getClass(), fieldName);
            if (f == null) {
                return null;
            }
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            log.warn("获取字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return null;
        }
    }

    /**
     * 在类及其父类中查找字段
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 导出字段元数据
     */
    @Getter
    private static class ExportFieldMeta {
        private final String field;
        private final String headerName;
        private final int sortIndex;

        ExportFieldMeta(String field, String headerName, int sortIndex) {
            this.field = field;
            this.headerName = headerName;
            this.sortIndex = sortIndex;
        }
    }
}
