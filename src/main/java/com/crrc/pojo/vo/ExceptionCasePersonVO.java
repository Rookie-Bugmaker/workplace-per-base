package com.crrc.pojo.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.ExceptionCasePerson;
import com.crrc.entity.ExceptionCasePersonRelation;
import lombok.Data;

import java.util.List;

@Data
public class ExceptionCasePersonVO {

    /**
     *  表头
     */
    private ExceptionCasePersonRelation relation;

    /**
     *  人员列表
     */
    private Page<ExceptionCasePerson> personPage;

    /**
     *  人员列表
     */
    private List<ExceptionCasePerson> personList;
}
