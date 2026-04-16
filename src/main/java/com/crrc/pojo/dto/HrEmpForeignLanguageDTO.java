package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpForeignLanguage;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpForeignLanguageDTO {
    String id;
    String userCode;
    List<HrEmpForeignLanguage> list;
}
