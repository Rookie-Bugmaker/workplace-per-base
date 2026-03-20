package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.BorrowedPersonnelApprovalForm;
import com.crrc.mapper.BorrowedPersonnelApprovalFormMapper;
import com.crrc.service.BorrowedPersonnelApprovalFormService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员审批明细表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
public class BorrowedPersonnelApprovalFormServiceImpl extends WorkServiceImpl<BorrowedPersonnelApprovalFormMapper, BorrowedPersonnelApprovalForm> implements BorrowedPersonnelApprovalFormService {

    @Override
    public List<BorrowedPersonnelApprovalForm> getByRelationId(String relationId) {
        LambdaQueryWrapper<BorrowedPersonnelApprovalForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowedPersonnelApprovalForm::getRelationId, relationId);
        return baseMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveDetails(String formId, List<BorrowedPersonnelApprovalForm> reqVOList) {
        List<BorrowedPersonnelApprovalForm> personnelSecondmentRequests = getByRelationId(formId);

        // 查询前端没有的传入
        Set<String> frontIds = reqVOList.stream().map(BorrowedPersonnelApprovalForm::getId).collect(Collectors.toSet());
        List<String> removeList = personnelSecondmentRequests.stream().map(BorrowedPersonnelApprovalForm::getId).filter(id -> !frontIds.contains(id)).collect(Collectors.toList());

        removeBatchByIds(removeList);

        return saveOrUpdateBatch(reqVOList);
    }
}