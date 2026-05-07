package com.crrc.enums;

import com.crrc.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 人员信息集导出类型枚举
 */
@Getter
@AllArgsConstructor
public enum HrEmpExportType {

    BASIC("basic", "基本信息", HrEmpBasic.class),
    EDUCATION("education", "教育经历", HrEmpEducation.class),
    CAREER_PATH("careerPath", "职业发展", HrEmpCareerPath.class),
    FAM_MEMBER("famMember", "家庭成员", HrEmpFamMember.class),
    PROFESSIONAL_QUALIFICATION("professionalQualification", "职业资格", HrEmpProfessionalQualification.class),
    SKILL_LEVEL("skillLevel", "职业技能等级", HrEmpSkillLevel.class),
    SALARY_INFO("salaryInfo", "薪酬信息", HrEmpSalaryInfo.class),
    SALARY_STANDARD("salaryStandard", "薪资标准", HrEmpSalaryStandard.class),
    PENALTIES("penalties", "惩处信息", HrEmpPenalties.class),
    PATENT("patent", "专利信息", HrEmpPatent.class),
    PAPER("paper", "论文论著", HrEmpPaper.class),
    APPOINTMENT("appointment", "任职信息", HrEmpAppointment.class),
    OTHER_APPOINTMENT("otherAppointment", "其他任职", HrEmpOtherAppointment.class),
    FOREIGN_LANGUAGE("foreignLanguage", "外语信息", HrEmpForeignLanguage.class),
    HONOR("honor", "荣誉信息", HrEmpHonor.class),
    RESUME("resume", "履历信息", HrEmpResume.class),
    REWARDS("rewards", "奖励信息", HrEmpRewards.class);

    private final String code;
    private final String sheetName;
    private final Class<?> entityClass;

    private static final Map<String, HrEmpExportType> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(HrEmpExportType::getCode, Function.identity()));

    public static HrEmpExportType fromCode(String code) {
        return CODE_MAP.get(code);
    }

    public static List<HrEmpExportType> allTypes() {
        return Arrays.asList(values());
    }
}
