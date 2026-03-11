package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpPaper;
import com.crrc.entity.HrEmpPatent;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpPatentDTO {
    String userCode;

    List<HrEmpPatent> list;
}
