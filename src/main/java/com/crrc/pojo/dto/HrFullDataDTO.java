package com.crrc.pojo.dto;

import com.crrc.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class HrFullDataDTO {
    HrEmpBasic hrEmpBasic;
    HrEmpAppointment hrEmpAppointment;
    HrEmpOtherAppointment hrEmpOtherAppointment;
    HrEmpProfessionalQualification hrEmpProQualification;
    HrEmpSkillLevel hrEmpSkillLevel;
    HrEmpCareerPath hrEmpCareerPath;
    HrEmpSalaryInfo hrEmpSalaryInfo;
    HrEmpSalaryStandard hrEmpSalaryStandard;
    List<HrEmpCadreAppointmentInfo> cadreAppointmentInfoList;
}
