package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpEducation;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class HrEmpEducationDTO {
    @Id
    String id;

    String userCode;

    List<HrEmpEducation> list;
}
