package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PersonnelSecondmentRequestRelation;

import java.util.List;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
public interface PersonnelSecondmentRequestRelationService extends IService<PersonnelSecondmentRequestRelation> {

    /**
     * 翻译字段
     * @param records 需要翻译的记录
     */
     void resolveFields(List<PersonnelSecondmentRequestRelation> records);
 }