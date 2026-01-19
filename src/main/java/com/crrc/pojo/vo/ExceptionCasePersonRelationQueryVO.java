package com.crrc.pojo.vo;

import com.crrc.entity.ExceptionCasePersonRelation;
import lombok.Data;

import java.util.List;

@Data
public class ExceptionCasePersonRelationQueryVO {
    List<String> containsSet;
    ExceptionCasePersonRelation pageReqVO;
}
