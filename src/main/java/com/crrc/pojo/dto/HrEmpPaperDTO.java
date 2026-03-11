package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPaper;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpPaperDTO {
    String userCode;

    List<HrEmpPaper> list;
}
