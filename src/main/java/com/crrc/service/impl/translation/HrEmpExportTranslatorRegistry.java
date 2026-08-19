package com.crrc.service.impl.translation;

import com.crrc.entity.*;
import com.crrc.pojo.vo.HrEmpBasicExportVO;
import com.crrc.service.CodeInfoService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.listener.HonorImportListener;
import com.crrc.utils.listener.PaperImportListener;
import com.crrc.utils.listener.PatentImportListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员信息导出翻译注册中心
 * 集中管理 (entityClass, fieldName) -> 翻译规则 的映射关系
 */
@Component
@Slf4j
public class HrEmpExportTranslatorRegistry {

    /** 注册表: entityClass -> fieldName -> rule */
    private final Map<Class<?>, Map<String, ExportFieldTranslationRule>> registry = new HashMap<>();

    /** 运行时缓存: "tableName:typeCode" -> Map<code, text> */
    private Map<String, Map<String, String>> codeTranslateMaps = Collections.emptyMap();

    /** 部门翻译缓存: orgCode -> orgName */
    private Map<String, String> deptTranslateMap = Collections.emptyMap();

    // ==================== 注册 ====================
    private Map<String, String> reverseMap(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,
                Map.Entry::getKey
        ));
    }

    @PostConstruct
    public void init() {
        // HrEmpBasicExportVO — 继承 HrEmpBasic，需要重新注册翻译规则（注册表以 Class 为 key）
        register(HrEmpBasicExportVO.class, "dept", ExportFieldTranslationRule.dept());
        register(HrEmpBasicExportVO.class, "gender", ExportFieldTranslationRule.genderFlag());
        register(HrEmpBasicExportVO.class, "nation", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_NATION"));
        register(HrEmpBasicExportVO.class, "nationality", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_NATIONALITY"));
        register(HrEmpBasicExportVO.class, "empSource", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_EMP_SOURCE"));
        register(HrEmpBasicExportVO.class, "maritalStatus", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_MARITAL_STATUS"));
        register(HrEmpBasicExportVO.class, "nativePlaceCode", ExportFieldTranslationRule.nativePlace());
        register(HrEmpBasicExportVO.class, "politicalValue", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_POLITICAL", "political"));
        register(HrEmpBasicExportVO.class, "status", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "per_status"));
        register(HrEmpBasicExportVO.class, "category1", ExportFieldTranslationRule.codeInfo("person_category", "category1"));
        register(HrEmpBasicExportVO.class, "category4", ExportFieldTranslationRule.codeInfo("person_category", "category4"));
        register(HrEmpBasicExportVO.class, "category7", ExportFieldTranslationRule.codeInfo("person_category", "category7"));
        register(HrEmpBasicExportVO.class, "category8", ExportFieldTranslationRule.codeInfo("person_category", "category8"));
        register(HrEmpBasicExportVO.class, "isEmployed", ExportFieldTranslationRule.employedFlag());
        register(HrEmpBasicExportVO.class, "position", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_POSITION"));
        register(HrEmpBasicExportVO.class, "certiType", ExportFieldTranslationRule.codeInfo("hr_emp_basic", "BASIC_CERTI_TYPE"));
        register(HrEmpBasicExportVO.class, "houseHoldType", ExportFieldTranslationRule.codeInfo("hr_emp_household", "HOUSRHOLD_TYPE"));
        register(HrEmpBasicExportVO.class, "healthStatus", ExportFieldTranslationRule.codeInfo("hr_emp_health", "BASIC_HEALTH_STATUS"));



        // HrEmpEducation
        register(HrEmpEducation.class, "major", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_MAJOR_CODE", "majorCode"));
        register(HrEmpEducation.class, "college", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_SCHOOL_CODE", "schoolCode"));
        register(HrEmpEducation.class, "isMajor", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpEducation.class, "isFullTime", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_FULL_TIME"));
        register(HrEmpEducation.class, "education", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_EDU"));
        register(HrEmpEducation.class, "degree", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_DEGREE"));
        register(HrEmpEducation.class, "isHighestDegree", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpEducation.class, "isHighestEducation", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpEducation.class, "completionStatus", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_COMPLETION_STATUS"));
        register(HrEmpEducation.class, "degreeMajor", ExportFieldTranslationRule.codeInfo("hr_emp_education", "EDU_DEGREE_MAJOR"));

        // HrEmpCareerPath
        register(HrEmpCareerPath.class, "pathId", ExportFieldTranslationRule.codeInfo("hr_emp_career_path", "CAREER_PATH_ID"));
        register(HrEmpCareerPath.class, "isExpired", ExportFieldTranslationRule.booleanFlag());

        // HrEmpFamMember
        register(HrEmpFamMember.class, "isEmergencyContact", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpFamMember.class, "famPoliticalStatus", ExportFieldTranslationRule.codeInfo("hr_emp_fam_member", "FAM_POLITICAL_STATUS"));
        register(HrEmpFamMember.class, "relation", ExportFieldTranslationRule.codeInfo("hr_emp_fam_member", "FAM_RELATION"));

        // HrEmpProfessionalQualification
        register(HrEmpProfessionalQualification.class, "quaId", ExportFieldTranslationRule.codeInfo("hr_emp_pro_qualification", "QUA_ID"));
        register(HrEmpProfessionalQualification.class, "quaTitleLevel", ExportFieldTranslationRule.codeInfo("hr_emp_pro_qualification", "QUA_LEVEL"));
        register(HrEmpProfessionalQualification.class, "quaPath", ExportFieldTranslationRule.codeInfo("hr_emp_pro_qualification", "QUA_PATH"));
        register(HrEmpProfessionalQualification.class, "quaApprovalUnit", ExportFieldTranslationRule.dept());
        register(HrEmpProfessionalQualification.class, "isHighest", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpProfessionalQualification.class, "isAppointed", ExportFieldTranslationRule.booleanFlag());

        // HrEmpSkillLevel
        register(HrEmpSkillLevel.class, "jobId", ExportFieldTranslationRule.codeInfo("hr_emp_skill_level", "SKILL_LEVEL_JOB_ID"));
        register(HrEmpSkillLevel.class, "skillLevel", ExportFieldTranslationRule.codeInfo("hr_emp_skill_level", "SKILL_LEVEL"));
        register(HrEmpSkillLevel.class, "isHighest", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpSkillLevel.class, "isAppointed", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpSkillLevel.class, "getMethod", ExportFieldTranslationRule.codeInfo("hr_emp_skill_level", "SKILL_LEVEL_GET_METHOD"));

        // HrEmpSalaryInfo - no confirmed dictionary mappings yet

        // HrEmpSalaryStandard - no confirmed dictionary mappings yet

        // HrEmpPenalties - no confirmed dictionary mappings yet

        // HrEmpPatent
        register(HrEmpPatent.class, "patentType", ExportFieldTranslationRule.map(reverseMap(PatentImportListener.patentTypeTranslateMap)));
        register(HrEmpPatent.class, "patentLevel", ExportFieldTranslationRule.map(reverseMap(PatentImportListener.patentLevelTranslateMap)));
        register(HrEmpPatent.class, "isAwarded", ExportFieldTranslationRule.booleanFlag());
        register(HrEmpPatent.class, "awardGrade", ExportFieldTranslationRule.map(reverseMap(PatentImportListener.awardGradeTranslateMap)));
        register(HrEmpPatent.class, "awardLevel", ExportFieldTranslationRule.map(reverseMap(PatentImportListener.awardLevelTranslateMap)));
        register(HrEmpPatent.class, "status", ExportFieldTranslationRule.map(reverseMap(PatentImportListener.patentStatusTranslateMap)));

        // HrEmpPaper
        register(HrEmpPaper.class, "paperType", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.typeTranslateMap)));
        register(HrEmpPaper.class, "isSciEiIstp", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.sciEiIstpTranslateMap)));
        register(HrEmpPaper.class, "isCoreJournal", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.coreJournalTranslateMap)));
        register(HrEmpPaper.class, "journalLevel", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.journalLevelTranslateMap)));
        register(HrEmpPaper.class, "awardLevel", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.awardLevelTranslateMap)));
        register(HrEmpPaper.class, "awardRank", ExportFieldTranslationRule.map(reverseMap(PaperImportListener.awardRankTranslateMap)));

        // HrEmpHonor
        register(HrEmpHonor.class, "titleType", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.titleTypeTranslateMap)));
        register(HrEmpHonor.class, "titleLevel", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.titleLevelTranslateMap)));
        register(HrEmpHonor.class, "groupOrPersonal", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.groupOrPersonalTranslateMap)));
        register(HrEmpHonor.class, "rewardType", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.rewardTypeTranslateMap)));
        register(HrEmpHonor.class, "responsibleUnit", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.responsibleUnitTranslateMap)));
        register(HrEmpHonor.class, "obtainMethod", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.obtainMethodTranslateMap)));
        register(HrEmpHonor.class, "category", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.categoryTranslateMap)));
        register(HrEmpHonor.class, "honorType", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.honorCategoriesTranslateMap)));
        register(HrEmpHonor.class, "isPersonInCharge", ExportFieldTranslationRule.map(reverseMap(HonorImportListener.isPersonInChargeTranslateMap)));

        // HrEmpAppointment
        register(HrEmpAppointment.class, "changeEvent", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_CHANGE_EVENT"));
        register(HrEmpAppointment.class, "changeType", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE"));
        register(HrEmpAppointment.class, "changeReason", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_CHANGE_REASON"));
        register(HrEmpAppointment.class, "org", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_ORG"));
        register(HrEmpAppointment.class, "dept", ExportFieldTranslationRule.dept());
        register(HrEmpAppointment.class, "position", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_POSITION"));
        register(HrEmpAppointment.class, "dutyStatus", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_DUTY_STATUS"));
        register(HrEmpAppointment.class, "positionCategory", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_POSITION_CATEGORY"));

        // HrEmpOtherAppointment
        register(HrEmpOtherAppointment.class, "org", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_ORG"));
        register(HrEmpOtherAppointment.class, "dept", ExportFieldTranslationRule.dept());
        register(HrEmpOtherAppointment.class, "positionCategory", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_POSITION_CATEGORY"));
        register(HrEmpOtherAppointment.class, "dutyStatus", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_DUTY_STATUS"));
        register(HrEmpOtherAppointment.class, "otherAppointmentType", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE"));

        // HrEmpForeignLanguage - no confirmed dictionary mappings yet

        // HrEmpResume
        register(HrEmpResume.class, "changeType", ExportFieldTranslationRule.codeInfo("hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE"));

        // HrEmpRewards - no confirmed dictionary mappings yet

        log.debug("导出翻译注册表初始化完成，共注册 {} 个字段规则",
                registry.values().stream().mapToInt(Map::size).sum());
    }

    private void register(Class<?> entityClass, String fieldName, ExportFieldTranslationRule rule) {
        registry.computeIfAbsent(entityClass, k -> new HashMap<>()).put(fieldName, rule);
    }

    // ==================== 预加载 ====================

    /**
     * 批量预加载翻译数据，在导出开始时调用
     */
    public void prefetchAll(Set<Class<?>> entityClasses, CodeInfoService codeInfoService) {
        // 收集需要加载的字典
        Set<String> dictKeys = new HashSet<>();
        boolean needDept = false;

        for (Class<?> entityClass : entityClasses) {
            Map<String, ExportFieldTranslationRule> fieldRules = registry.get(entityClass);
            if (fieldRules == null) continue;
            for (ExportFieldTranslationRule rule : fieldRules.values()) {
                if (rule.getType() == ExportFieldTranslationRule.Type.CODE_INFO || rule.getType() == ExportFieldTranslationRule.Type.NATIVE_PLACE) {
                    dictKeys.add(rule.getTableName() + ":" + rule.getTypeCode());
                } else if (rule.getType() == ExportFieldTranslationRule.Type.DEPT) {
                    needDept = true;
                }
                // BOOLEAN_FLAG 和 EMPLOYED_FLAG 不需要预加载
            }
        }

        // 批量加载字典
        Map<String, Map<String, String>> maps = new HashMap<>();
        for (String key : dictKeys) {
            int sep = key.indexOf(':');
            String tableName = key.substring(0, sep);
            String typeCode = key.substring(sep + 1);
            try {
                Map<String, String> map = codeInfoService.getTranslateMap(tableName, typeCode);
                maps.put(key, map != null ? map : Collections.emptyMap());
            } catch (Exception e) {
                log.warn("加载字典翻译失败: tableName={}, typeCode={}", tableName, typeCode, e);
                maps.put(key, Collections.emptyMap());
            }
        }

        // 加载部门翻译
        Map<String, String> deptMap = Collections.emptyMap();
        if (needDept) {
            try {
                deptMap = DeptUtils.getDeptTranslateMap();
            } catch (Exception e) {
                log.warn("加载部门翻译失败", e);
            }
        }

        this.codeTranslateMaps = maps;
        this.deptTranslateMap = deptMap;
    }

    // ==================== 翻译 ====================

    /**
     * 判断当前字段是否需要翻译
     */
    public boolean needsTranslation(Class<?> entityClass, String fieldName) {
        Map<String, ExportFieldTranslationRule> fieldRules = registry.get(entityClass);
        return fieldRules != null && fieldRules.containsKey(fieldName);
    }

    /**
     * 翻译字段值
     * @param entityClass 实体类
     * @param fieldName 导出字段名（即 @ExcelProperty 所在字段）
     * @param rawValue 当前字段值（可能为 null）
     * @param rowData 当前行对象（用于取 sourceField 的值）
     * @return 翻译后的值，无翻译规则或翻译失败时返回原始值
     */
    public Object translate(Class<?> entityClass, String fieldName, Object rawValue, Object rowData) {
        Map<String, ExportFieldTranslationRule> fieldRules = registry.get(entityClass);
        if (fieldRules == null) {
            return rawValue;
        }
        ExportFieldTranslationRule rule = fieldRules.get(fieldName);
        if (rule == null) {
            return rawValue;
        }

        // null 值直接返回
        if (rawValue == null) {
            return null;
        }

        String rawStr = rawValue.toString();
        // 空字符串直接返回
        if (rawStr.isEmpty()) {
            return "";
        }

        try {
            switch (rule.getType()) {
                case CODE_INFO:
                    return translateCodeInfo(rule, rawStr, rowData);
                case MAP:
                    return translateMap(rule, rawStr, rowData);
                case DEPT:
                    return translateDept(rawStr);
                case BOOLEAN_FLAG:
                    return translateBooleanFlag(rawStr);
                case EMPLOYED_FLAG:
                    return translateEmployedFlag(rawStr);
                case GENDER_FLAG:
                    return translateGenderFlag(rawStr);
                case NATIVE_PLACE:
                    return translateNaivePlace(rule, rawStr, rowData);
                default:
                    return rawValue;
            }
        } catch (Exception e) {
            log.debug("字段翻译失败: entity={}, field={}, value={}",
                    entityClass.getSimpleName(), fieldName, rawValue, e);
            return rawValue;
        }
    }

    private String translateMap(ExportFieldTranslationRule rule, String rawValue, Object rowData) {
        String codeValue = extractCodeValue(rule, rawValue, rowData);
        if (codeValue == null || codeValue.isEmpty()) {
            return codeValue;
        }

        return rule.getTranslateMapCollection().getOrDefault(codeValue, codeValue);
    }

    private String translateNaivePlace(ExportFieldTranslationRule rule, String rawValue, Object rawData) {
        // 如果有 sourceField，从源字段取原始码值
        String codeValue = extractCodeValue(rule, rawValue, rawData);
        if (codeValue == null || codeValue.isEmpty()) {
            return codeValue;
        }

        String[] split = codeValue.split(",");
        return Arrays.stream(split).map(item -> translateCodeInfo(rule, item, rawData)).collect(Collectors.joining());
    }

    private String extractCodeValue(ExportFieldTranslationRule rule, String rawValue, Object rawData) {
        String codeValue = rawValue;
        if (rule.getSourceField() != null && rawData != null) {
            Object sourceVal = reflectFieldValue(rawData, rule.getSourceField());
            if (sourceVal != null) {
                codeValue = sourceVal.toString();
            }
        }
        return codeValue;
    }

    private String translateGenderFlag(String rawValue) {
        if ("1".equals(rawValue)) return "男";
        if ("0".equals(rawValue)) return "女";
        return rawValue;
    }

    /**
     * CodeInfo 字典翻译
     */
    private String translateCodeInfo(ExportFieldTranslationRule rule, String rawValue, Object rowData) {
        // 如果有 sourceField，从源字段取原始码值
        String codeValue = extractCodeValue(rule, rawValue, rowData);
        if (codeValue == null || codeValue.isEmpty()) {
            return codeValue;
        }

        String dictKey = rule.getTableName() + ":" + rule.getTypeCode();
        Map<String, String> dictMap = codeTranslateMaps.get(dictKey);
        if (dictMap == null) {
            return codeValue;
        }
        return dictMap.getOrDefault(codeValue, codeValue);
    }

    /**
     * 部门/组织翻译
     */
    private String translateDept(String rawValue) {
        if (deptTranslateMap == null || deptTranslateMap.isEmpty()) {
            return rawValue;
        }
        return deptTranslateMap.getOrDefault(rawValue, rawValue);
    }

    /**
     * 布尔标志翻译: 1 -> 是, 0 -> 否
     */
    private String translateBooleanFlag(String rawValue) {
        if ("1".equals(rawValue)) return "是";
        if ("0".equals(rawValue)) return "否";
        return rawValue;
    }

    /**
     * 离职标记翻译: 1 -> 在职, 0 -> 离职
     */
    private String translateEmployedFlag(String rawValue) {
        if ("1".equals(rawValue)) return "在职";
        if ("0".equals(rawValue)) return "离职";
        return rawValue;
    }

    /**
     * 反射获取对象字段值
     */
    private Object reflectFieldValue(Object obj, String fieldName) {
        Class<?> current = obj.getClass();
        while (current != null && current != Object.class) {
            try {
                java.lang.reflect.Field f = current.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 重置缓存（用于测试或导出间隔清理）
     */
    public void resetCache() {
        this.codeTranslateMaps = Collections.emptyMap();
        this.deptTranslateMap = Collections.emptyMap();
    }
}
