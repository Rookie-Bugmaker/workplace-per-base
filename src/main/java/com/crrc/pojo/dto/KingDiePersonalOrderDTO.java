package com.crrc.pojo.dto;

import com.crrc.annotation.QueryMethod;
import com.crrc.entity.*;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用于接收金蝶回传的人事令变更信息
 */
@Data
public class KingDiePersonalOrderDTO {

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "任职信息")
    private HrEmpAppointment hrEmpAppointment;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "员工基本信息表")
    private HrEmpBasic hrEmpBasic;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职业发展通道")
    private HrEmpCareerPath hrEmpCareerPath;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "其他任职信息")
    private HrEmpOtherAppointment hrEmpOtherAppointment;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "员工惩处信息表")
    private HrEmpPenalties hrEmpPenalties;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "员工专业职务任职资格表")
    private HrEmpProfessionalQualification hrEmpProfessionalQualification;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "员工履历信息表")
    private HrEmpResume hrEmpResume;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "员工奖励情况表")
    private HrEmpRewards hrEmpRewards;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "薪资标准")
    private HrEmpSalaryStandard hrEmpSalaryStandard;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职业技能等级")
    private HrEmpSkillLevel hrEmpSkillLevel;
}
