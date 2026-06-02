package com.crrc.pojo.vo;

import com.crrc.annotation.WordTemplateRequire;
import lombok.Data;

/**
 * <p>
 * 干部任免审批表 - 模板标量字段
 * 字段名即占位符（@WordTemplateRequire 默认 ${字段名}），需与模板占位符一一对应。
 * 注意：简历(${resume})、奖惩(${rewards})、照片(${photo})、家庭成员(${fam*N})
 *       不在本 VO 中，由服务单独处理（多行/图片/动态行）。
 * </p>
 */
@Data
public class HrEmpCadreAppointmentFormVO {

    @WordTemplateRequire
    private String name;
    @WordTemplateRequire
    private String gender;
    /** 出生年月 yyyy.MM */
    @WordTemplateRequire
    private String birthYm;
    /** 年龄数字，模板中为 (${age}岁) */
    @WordTemplateRequire
    private String age;
    @WordTemplateRequire
    private String nation;
    /** 籍贯 */
    @WordTemplateRequire
    private String nativePlace;
    /** 出生地 */
    @WordTemplateRequire
    private String birthPlace;
    /** 入党时间 yyyy.MM */
    @WordTemplateRequire
    private String partyJoinYm;
    /** 参加工作时间 yyyy.MM */
    @WordTemplateRequire
    private String workYm;
    @WordTemplateRequire
    private String health;
    /** 专业技术职务 */
    @WordTemplateRequire
    private String professionalTitle;

    /** 学历学位-全日制：学历 / 学位 / 毕业院校系 / 专业 */
    @WordTemplateRequire
    private String fulltimeEducation;
    @WordTemplateRequire
    private String fulltimeDegree;
    @WordTemplateRequire
    private String fulltimeSchool;
    @WordTemplateRequire
    private String fulltimeMajor;

    /** 学历学位-在职：学历 / 学位 / 毕业院校系 / 专业（无则空） */
    @WordTemplateRequire
    private String parttimeEducation;
    @WordTemplateRequire
    private String parttimeDegree;
    @WordTemplateRequire
    private String parttimeSchool;
    @WordTemplateRequire
    private String parttimeMajor;

    /** 现任职务 */
    @WordTemplateRequire
    private String currentPosition;

    /** 年度考核结果（仅填年份，等级留空） */
    @WordTemplateRequire
    private String assessment;
    /** 任免理由（默认：工作需要。） */
    @WordTemplateRequire
    private String appointmentReason;
    /** 呈报单位（固定） */
    @WordTemplateRequire
    private String reportUnit;
    /** 呈报日期 yyyy年M月d日（导出当天） */
    @WordTemplateRequire
    private String reportDate;
    /** 填表人（导出人） */
    @WordTemplateRequire
    private String formFiller;
}
