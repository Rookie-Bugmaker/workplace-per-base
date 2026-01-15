package com.crrc.pojo.dto;

import com.crrc.entity.ExceptionCasePerson;
import com.crrc.entity.ExceptionCasePersonRelation;
import lombok.Data;

import java.util.List;

@Data
public class ExceptionCasePersonDTO {
    /**
     *  表头
     */
    private ExceptionCasePersonRelation relation;

    /**
     *  人员列表
     */
    private List<ExceptionCasePerson> personList;
}
