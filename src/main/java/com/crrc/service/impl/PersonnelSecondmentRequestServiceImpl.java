package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.mapper.PersonnelSecondmentRequestMapper;
import com.crrc.service.PersonnelSecondmentRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员需求计划申请明细表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
public class PersonnelSecondmentRequestServiceImpl extends WorkServiceImpl<PersonnelSecondmentRequestMapper, PersonnelSecondmentRequest> implements PersonnelSecondmentRequestService {

    public List<PersonnelSecondmentRequest> getByRelationId(String relationId) {
        LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonnelSecondmentRequest::getRelationId, relationId);
        return baseMapper.selectList(wrapper);
    }

    public boolean saveDetails(String relationId, List<PersonnelSecondmentRequest> reqVOList) {
        List<PersonnelSecondmentRequest> personnelSecondmentRequests = getByRelationId(relationId);

        // 查询前端没有的传入
        Set<String> frontIds = reqVOList.stream().map(PersonnelSecondmentRequest::getId).collect(Collectors.toSet());
        List<String> removeList = personnelSecondmentRequests.stream().map(PersonnelSecondmentRequest::getId).filter(id -> !frontIds.contains(id)).collect(Collectors.toList());

        removeBatchByIds(removeList);

        return saveOrUpdateBatch(reqVOList);
    }
}