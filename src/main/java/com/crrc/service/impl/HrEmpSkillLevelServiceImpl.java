package com.crrc.service.impl;

import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.mapper.HrEmpSkillLevelMapper;
import com.crrc.service.HrEmpSkillLevelService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 职业技能等级 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpSkillLevelServiceImpl extends WorkServiceImpl<HrEmpSkillLevelMapper, HrEmpSkillLevel> implements HrEmpSkillLevelService {

    @Autowired
    private HrEmpSkillLevelMapper hrEmpSkillLevelMapper;

    public List<HrEmpSkillLevel> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpSkillLevel> wrapper = new MPJLambdaWrapper<HrEmpSkillLevel>()
                .selectAll(HrEmpSkillLevel.class)
                .eq(HrEmpSkillLevel::getUserCode, userCode);
        return hrEmpSkillLevelMapper.selectList(wrapper);
    }

}