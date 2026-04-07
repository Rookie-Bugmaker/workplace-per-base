package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpFamMember;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class HrEmpFamMemberDTO {
    @Id
    String id;
    String userCode;
    List<HrEmpFamMember> list;
}
