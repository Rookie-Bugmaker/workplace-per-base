package com.crrc.service.impl;

import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpHealth;
import com.crrc.mapper.HrEmpHealthMapper;
import com.crrc.service.HrEmpHealthService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工健康信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpHealthServiceImpl extends WorkServiceImpl<HrEmpHealthMapper, HrEmpHealth> implements HrEmpHealthService {

    @Autowired
    private HrEmpHealthMapper hrEmpHealthMapper;


    public HrEmpHealth selectByUserCode(String userCode) {
        MPJLambdaWrapper<HrEmpHealth> queryWrapper = new MPJLambdaWrapper<>(HrEmpHealth.class)
                .selectAll(HrEmpHealth.class)
                .eq(HrEmpHealth::getUserCode, userCode);
        return hrEmpHealthMapper.selectOne(queryWrapper);
    }

}