package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpFamMember;
import lombok.Data;

import java.util.List;

@Data
public class HrEmpFamMemberDTO {
    String userCode;
    List<HrEmpFamMember> list;
}
