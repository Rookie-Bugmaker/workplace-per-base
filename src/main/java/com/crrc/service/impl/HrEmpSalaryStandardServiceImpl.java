package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpSalaryStandard;
import com.crrc.mapper.HrEmpSalaryStandardMapper;
import com.crrc.service.HrEmpSalaryStandardService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 薪资标准 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpSalaryStandardServiceImpl extends WorkServiceImpl<HrEmpSalaryStandardMapper, HrEmpSalaryStandard> implements HrEmpSalaryStandardService {

    @Autowired
    private HrEmpSalaryStandardMapper hrEmpSalaryStandardMapper;

    public List<HrEmpSalaryStandard> selectMsg(String userCode) {
        LambdaQueryWrapper<HrEmpSalaryStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(HrEmpSalaryStandard::getUserCode, userCode)
                // 添加条件：薪档,薪级,执行系数/金额同时未null时，不进行查询
                .not(innerWrapper -> innerWrapper
                        .isNull(HrEmpSalaryStandard::getSalaryGrade)
                        .isNull(HrEmpSalaryStandard::getSalaryLevel)
                        .isNull(HrEmpSalaryStandard::getCoefficient)
                );

        wrapper.last("ORDER BY create_time DESC NULLS LAST");
        return hrEmpSalaryStandardMapper.selectList(wrapper);
    }
}