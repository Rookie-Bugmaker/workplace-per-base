package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.ExceptionCasePersonRelation;
import com.crrc.mapper.ExceptionCasePersonRelationMapper;
import com.crrc.pojo.vo.ExceptionCasePersonRelationQueryVO;
import com.crrc.service.ExceptionCasePersonRelationService;
import kotlin.jvm.internal.Lambda;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 额外人员信息关联表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@Service
public class ExceptionCasePersonRelationServiceImpl extends WorkServiceImpl<ExceptionCasePersonRelationMapper, ExceptionCasePersonRelation> implements ExceptionCasePersonRelationService {

    @Override
    public void queryByVO(Page<ExceptionCasePersonRelation> page, ExceptionCasePersonRelationQueryVO queryVO) {
        LambdaQueryWrapper<ExceptionCasePersonRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(!ObjectUtils.isEmpty(queryVO.getContainsSet()), ExceptionCasePersonRelation::getUserType, queryVO.getContainsSet());
        wrapper.orderByDesc(ExceptionCasePersonRelation::getCreateTime);
        baseMapper.selectPage(page, wrapper);
    }
}