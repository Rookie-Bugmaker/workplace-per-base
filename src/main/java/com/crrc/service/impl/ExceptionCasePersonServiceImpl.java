package com.crrc.service.impl;

import com.crrc.entity.ExceptionCasePerson;
import com.crrc.mapper.ExceptionCasePersonMapper;
import com.crrc.service.ExceptionCasePersonService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 额外人员信息表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@Service
public class ExceptionCasePersonServiceImpl extends WorkServiceImpl<ExceptionCasePersonMapper, ExceptionCasePerson> implements ExceptionCasePersonService {

    @Autowired
    private ExceptionCasePersonMapper exceptionCasePersonMapper;

    public List<ExceptionCasePerson> selectByRelationId(String relationId) {
        MPJLambdaWrapper<ExceptionCasePerson> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectAll(ExceptionCasePerson.class)
                .eq(ExceptionCasePerson::getRelationId, relationId);
        return exceptionCasePersonMapper.selectList(wrapper);
    }

}