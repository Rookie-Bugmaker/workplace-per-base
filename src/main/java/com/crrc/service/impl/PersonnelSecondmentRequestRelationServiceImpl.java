package com.crrc.service.impl;

import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.service.PersonnelSecondmentRequestRelationService;
import com.crrc.utils.DeptUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
public class PersonnelSecondmentRequestRelationServiceImpl extends WorkServiceImpl<PersonnelSecondmentRequestRelationMapper, PersonnelSecondmentRequestRelation> implements PersonnelSecondmentRequestRelationService {

    @Override
    public void resolveFields(List<PersonnelSecondmentRequestRelation> records) {
        Map<String, String> translateMap = records.stream().map(PersonnelSecondmentRequestRelation::getRequestingDepartment).distinct().collect(Collectors.toMap(
                k -> k,
                DeptUtils::getDeptNameByCode,
                (a, b) -> a
        ));

        records.forEach(item ->
            item.setRequestingDepartmentValue(
                translateMap.getOrDefault(item.getRequestingDepartment(), item.getRequestingDepartment())
            )
        );
    }
}