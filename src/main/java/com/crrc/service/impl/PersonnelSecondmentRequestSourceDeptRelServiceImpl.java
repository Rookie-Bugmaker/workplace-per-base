package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.PersonnelSecondmentRequestSourceDeptRel;
import com.crrc.mapper.PersonnelSecondmentRequestSourceDeptRelMapper;
import com.crrc.service.PersonnelSecondmentRequestSourceDeptRelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonnelSecondmentRequestSourceDeptRelServiceImpl extends WorkServiceImpl<PersonnelSecondmentRequestSourceDeptRelMapper, PersonnelSecondmentRequestSourceDeptRel> implements PersonnelSecondmentRequestSourceDeptRelService {

    public List<PersonnelSecondmentRequestSourceDeptRel> getListByRelationId(String relationId) {
        LambdaQueryWrapper<PersonnelSecondmentRequestSourceDeptRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonnelSecondmentRequestSourceDeptRel::getRelationId, relationId);
        return baseMapper.selectList(wrapper);
    }
}
