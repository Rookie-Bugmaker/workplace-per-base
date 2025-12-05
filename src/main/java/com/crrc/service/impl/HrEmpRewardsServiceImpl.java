package com.crrc.service.impl;

import com.crrc.entity.HrEmpRewards;
import com.crrc.mapper.HrEmpRewardsMapper;
import com.crrc.service.HrEmpRewardsService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工奖励情况表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpRewardsServiceImpl extends WorkServiceImpl<HrEmpRewardsMapper, HrEmpRewards> implements HrEmpRewardsService {

    @Autowired
    private HrEmpRewardsMapper hrEmpRewardsMapper;

    public List<HrEmpRewards> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpRewards> wrapper = new MPJLambdaWrapper<HrEmpRewards>()
                .selectAll(HrEmpRewards.class)
                .eq(HrEmpRewards::getUserCode, userCode);
        return hrEmpRewardsMapper.selectList(wrapper);
    }

}