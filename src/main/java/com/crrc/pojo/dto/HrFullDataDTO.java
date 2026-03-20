package com.crrc.pojo.dto;

import com.crrc.entity.*;
import lombok.Data;

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
}
