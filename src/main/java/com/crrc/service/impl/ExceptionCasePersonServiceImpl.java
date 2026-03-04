package com.crrc.service.impl;

import com.crrc.entity.ExceptionCasePerson;
import com.crrc.entity.ExceptionCasePersonRelation;
import com.crrc.mapper.ExceptionCasePersonMapper;
import com.crrc.pojo.vo.ExceptionCasePersonPlusVO;
import com.crrc.service.ExceptionCasePersonService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    /**
     * 查询当前有效的调动人员及其对应的调动类型
     * @return 包含调动类型的人员列表
     */
    public List<ExceptionCasePersonPlusVO> selectEffectivePerson() {
        MPJLambdaWrapper<ExceptionCasePerson> wrapper = new MPJLambdaWrapper<>();
        LocalDate currentDate = LocalDate.now();

        wrapper.selectAll(ExceptionCasePerson.class)
                .select(ExceptionCasePersonRelation::getUserType)
                .leftJoin(ExceptionCasePersonRelation.class,
                        ExceptionCasePersonRelation::getId,
                        ExceptionCasePerson::getRelationId)
                .eq(ExceptionCasePerson::getDeleted, "0")
                .le(ExceptionCasePerson::getStartDate, currentDate)
                .ge(ExceptionCasePerson::getEndDate, currentDate);

        return exceptionCasePersonMapper.selectJoinList(ExceptionCasePersonPlusVO.class, wrapper);
    }
}