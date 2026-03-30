package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpHonor;
import com.crrc.entity.HrEmpPatent;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpHonorDTO {
    String userCode;
    String responsibleUint;
    List<HrEmpHonor> list;
}
