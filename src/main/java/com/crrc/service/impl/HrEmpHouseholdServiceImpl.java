package com.crrc.service.impl;

import com.crrc.entity.HrEmpHouseHold;
import com.crrc.mapper.HrEmpHouseholdMapper;
import com.crrc.service.HrEmpHouseholdService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工户口本信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpHouseholdServiceImpl extends WorkServiceImpl<HrEmpHouseholdMapper, HrEmpHouseHold> implements HrEmpHouseholdService {
    @Autowired
    private HrEmpHouseholdMapper hrEmpHouseholdMapper;

    public HrEmpHouseHold selectByUserCode(String userCode) {
        MPJLambdaWrapper<HrEmpHouseHold> queryWrapper = new MPJLambdaWrapper<>(HrEmpHouseHold.class)
                .selectAll(HrEmpHouseHold.class)
                .eq(HrEmpHouseHold::getUserCode, userCode);
        return hrEmpHouseholdMapper.selectOne(queryWrapper);
    }

}