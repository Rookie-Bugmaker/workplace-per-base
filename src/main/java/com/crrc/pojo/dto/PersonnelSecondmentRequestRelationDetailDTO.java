package com.crrc.pojo.dto;

import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import lombok.Data;

import java.util.List;

@Data
public class PersonnelSecondmentRequestRelationDetailDTO {
    PersonnelSecondmentRequestRelation relation;
    List<PersonnelSecondmentRequest> requestList;
}
