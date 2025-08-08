package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpEducation;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpEducationDTO {
    String userCode;

    List<HrEmpEducation> list;
}
