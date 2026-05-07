package com.crrc.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.*;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.HrEmpExportType;
import com.crrc.excel.YearMonthStringConverter;
import com.crrc.mapper.*;
import com.crrc.pojo.dto.HrEmpBatchExportRequest;
import com.crrc.service.CodeInfoService;
import com.crrc.service.ExportProgressUpdater;
import com.crrc.service.HrEmpExportService;
import com.crrc.service.impl.translation.HrEmpExportTranslatorRegistry;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.HrEmpExportMergeStrategy;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;
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
    private CodeInfoService codeInfoService;
    @Autowired
    private HrEmpExportTranslatorRegistry translatorRegistry;

    @Override
    public byte[] exportHrEmpInfo(HrEmpBatchExportRequest request, ExportProgressUpdater progressUpdater) {
        // 1. 查询人员基本信息
        List<HrEmpBasic> basicList = queryBasicList(request);
        List<String> userCodes = basicList.stream().map(HrEmpBasic::getUserCode).collect(Collectors.toList());

        // 2. 确定要导出的信息集
        List<HrEmpExportType> exportTypes = resolveExportTypes(request.getExportTypes());

        // 3. 预加载翻译数据
        Set<Class<?>> entityClasses = exportTypes.stream()
                .map(HrEmpExportType::getEntityClass)
                .collect(Collectors.toSet());
        translatorRegistry.prefetchAll(entityClasses, codeInfoService);

        // 4. 计算 totalCount（主表记录数 + 各个子表记录数）
        int totalCount = basicList.size(); // 基本信息行
        for (HrEmpExportType exportType : exportTypes) {
            if (exportType != HrEmpExportType.BASIC) {
                List<?> subData = querySubTable(exportType, userCodes);
                totalCount += subData.size();
            }
        }

        // 初始化进度
        if (progressUpdater != null) {
            progressUpdater.initTotal(totalCount);
            // 基本信息行已处理
            progressUpdater.increment(basicList.size());
        }

        // 5. 生成 Excel 到 ByteArrayOutputStream
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
                } else {
                    List<?> subData = querySubTable(exportType, userCodes);
                    writeSubTableSheetWithProgress(excelWriter, exportType, basicList, userCodes, subData, progressUpdater);
                }
            }
        } finally {
            excelWriter.finish();
        }

        return baos.toByteArray();
    }

    /**
     * 查询符合条件的人员基本信息
     */
    private List<HrEmpBasic> queryBasicList(HrEmpBatchExportRequest request) {
        MPJLambdaWrapper<HrEmpBasic> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectAll(HrEmpBasic.class);

        if (StringUtils.isNotBlank(request.getName())) {
            wrapper.like(HrEmpBasic::getName, request.getName());
        }
        if (StringUtils.isNotBlank(request.getUserCode())) {
            wrapper.like(HrEmpBasic::getUserCode, request.getUserCode());
        }

        if (StringUtils.isNotBlank(request.getDeptCode())) {
            Set<String> deptSet = DeptUtils.childrenOrganization(request.getDeptCode(), true)
                    .stream()
                    .map(OrganizationTreeDTO::getOrgCode)
                    .collect(Collectors.toSet());
            wrapper.in(HrEmpBasic::getDept, deptSet);
        }

        return hrEmpBasicMapper.selectList(wrapper);
    }

    /**
     * 确定要导出的信息集列表
     */
    private List<HrEmpExportType> resolveExportTypes(List<String> exportTypes) {
        if (exportTypes == null || exportTypes.isEmpty()) {
            return HrEmpExportType.allTypes();
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
    private void writeBasicSheet(ExcelWriter excelWriter, List<HrEmpBasic> basicList) {
        Class<?> entityClass = HrEmpBasic.class;
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
    private void writeSubTableSheetWithProgress(ExcelWriter excelWriter, HrEmpExportType exportType,
                                                 List<HrEmpBasic> basicList, List<String> userCodes,
                                                 List<?> subData, ExportProgressUpdater progressUpdater) {
        Class<?> entityClass = exportType.getEntityClass();
        List<ExportFieldMeta> fieldMetas = extractFieldMetas(entityClass);

        // 构建 userCode -> 子表记录列表 的映射
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

        // 表头：姓名 + 档案号 + 子表业务字段
        List<List<String>> head = buildBasicHead();
        for (ExportFieldMeta meta : fieldMetas) {
            head.add(Collections.singletonList(meta.getHeaderName()));
        }

        // 数据：按 basicList 顺序展开
        List<List<Object>> data = new ArrayList<>();
        List<String> rowUserCodes = new ArrayList<>();
        int batchCounter = 0;
        final int BATCH_SIZE = 100;

        for (HrEmpBasic basic : basicList) {
            List<Object> subItems = subMap.getOrDefault(basic.getUserCode(), Collections.emptyList());
            if (!subItems.isEmpty()) {
                for (Object subItem : subItems) {
                    List<Object> row = new ArrayList<>();
                    row.add(basic.getName());
                    row.add(basic.getUserCode());
                    for (ExportFieldMeta meta : fieldMetas) {
                        Object rawValue = getFieldValue(subItem, meta.getField());
                        Object exportValue = translatorRegistry.translate(entityClass, meta.getField(), rawValue, subItem);
                        row.add(exportValue);
                    }
                    data.add(row);
                    rowUserCodes.add(basic.getUserCode());

                    // 每 BATCH_SIZE 条更新一次进度
                    batchCounter++;
                    if (progressUpdater != null && batchCounter % BATCH_SIZE == 0) {
                        progressUpdater.increment(batchCounter);
                        batchCounter = 0;
                    }
                }
            }
        }

        // 更新剩余进度
        if (progressUpdater != null && batchCounter > 0) {
            progressUpdater.increment(batchCounter);
        }

        // 构建合并策略
        HrEmpExportMergeStrategy mergeStrategy = new HrEmpExportMergeStrategy(rowUserCodes);

        WriteSheet writeSheet = EasyExcelFactory.writerSheet(exportType.getSheetName())
                .head(head)
                .registerWriteHandler(mergeStrategy)
                .build();
        excelWriter.write(data, writeSheet);
    }

    /**
     * 查询子表数据
     */
    private List<?> querySubTable(HrEmpExportType exportType, List<String> userCodes) {
        if (userCodes == null || userCodes.isEmpty()) {
            return Collections.emptyList();
        }
        Class<?> entityClass = exportType.getEntityClass();

        if (entityClass == HrEmpEducation.class) {
            LambdaQueryWrapper<HrEmpEducation> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpEducation::getUserCode, userCodes);
            return hrEmpEducationMapper.selectList(wrapper);
        } else if (entityClass == HrEmpCareerPath.class) {
            LambdaQueryWrapper<HrEmpCareerPath> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpCareerPath::getUserCode, userCodes);
            return hrEmpCareerPathMapper.selectList(wrapper);
        } else if (entityClass == HrEmpFamMember.class) {
            LambdaQueryWrapper<HrEmpFamMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpFamMember::getUserCode, userCodes);
            return hrEmpFamMemberMapper.selectList(wrapper);
        } else if (entityClass == HrEmpProfessionalQualification.class) {
            LambdaQueryWrapper<HrEmpProfessionalQualification> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpProfessionalQualification::getUserCode, userCodes);
            return hrEmpProQualificationMapper.selectList(wrapper);
        } else if (entityClass == HrEmpSkillLevel.class) {
            LambdaQueryWrapper<HrEmpSkillLevel> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpSkillLevel::getUserCode, userCodes);
            return hrEmpSkillLevelMapper.selectList(wrapper);
        } else if (entityClass == HrEmpSalaryInfo.class) {
            LambdaQueryWrapper<HrEmpSalaryInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpSalaryInfo::getUserCode, userCodes);
            return hrEmpSalaryInfoMapper.selectList(wrapper);
        } else if (entityClass == HrEmpSalaryStandard.class) {
            LambdaQueryWrapper<HrEmpSalaryStandard> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpSalaryStandard::getUserCode, userCodes);
            return hrEmpSalaryStandardMapper.selectList(wrapper);
        } else if (entityClass == HrEmpPenalties.class) {
            LambdaQueryWrapper<HrEmpPenalties> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpPenalties::getUserCode, userCodes);
            return hrEmpPenaltiesMapper.selectList(wrapper);
        } else if (entityClass == HrEmpPatent.class) {
            LambdaQueryWrapper<HrEmpPatent> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpPatent::getUserCode, userCodes);
            return hrEmpPatentMapper.selectList(wrapper);
        } else if (entityClass == HrEmpPaper.class) {
            LambdaQueryWrapper<HrEmpPaper> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpPaper::getUserCode, userCodes);
            return hrEmpPaperMapper.selectList(wrapper);
        } else if (entityClass == HrEmpAppointment.class) {
            LambdaQueryWrapper<HrEmpAppointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpAppointment::getUserCode, userCodes);
            return hrEmpAppointmentMapper.selectList(wrapper);
        } else if (entityClass == HrEmpOtherAppointment.class) {
            LambdaQueryWrapper<HrEmpOtherAppointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpOtherAppointment::getUserCode, userCodes);
            return hrEmpOtherAppointmentMapper.selectList(wrapper);
        } else if (entityClass == HrEmpForeignLanguage.class) {
            LambdaQueryWrapper<HrEmpForeignLanguage> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpForeignLanguage::getUserCode, userCodes);
            return hrEmpForeignLanguageMapper.selectList(wrapper);
        } else if (entityClass == HrEmpHonor.class) {
            LambdaQueryWrapper<HrEmpHonor> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpHonor::getUserCode, userCodes);
            return hrEmpHonorMapper.selectList(wrapper);
        } else if (entityClass == HrEmpResume.class) {
            LambdaQueryWrapper<HrEmpResume> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpResume::getUserCode, userCodes);
            return hrEmpResumeMapper.selectList(wrapper);
        } else if (entityClass == HrEmpRewards.class) {
            LambdaQueryWrapper<HrEmpRewards> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(HrEmpRewards::getUserCode, userCodes);
            return hrEmpRewardsMapper.selectList(wrapper);
        }
        return Collections.emptyList();
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
     * 从实体类提取可导出的字段元数据
     */
    private List<ExportFieldMeta> extractFieldMetas(Class<?> entityClass) {
        List<ExportFieldMeta> metas = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();

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
