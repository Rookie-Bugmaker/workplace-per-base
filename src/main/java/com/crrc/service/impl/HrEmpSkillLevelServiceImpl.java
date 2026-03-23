package com.crrc.service.impl;

import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.mapper.HrEmpSkillLevelMapper;
import com.crrc.service.HrEmpSkillLevelService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    public List<HrEmpSkillLevel> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpSkillLevel> wrapper = new MPJLambdaWrapper<HrEmpSkillLevel>()
                .selectAll(HrEmpSkillLevel.class)
                .eq(HrEmpSkillLevel::getUserCode, userCode);
        return hrEmpSkillLevelMapper.selectList(wrapper);
    }

    @Override
    public void resolveDetails(List<HrEmpSkillLevel> hrEmpSkillLevels) {
        for(HrEmpSkillLevel level : hrEmpSkillLevels) {
            // 处理聘任结束时间，如果在2098年之后的，置为空
            if(level.getAppointedEndDate() != null && level.getAppointedEndDate().isAfter(LocalDate.parse("2098-01-01"))) {
                level.setAppointedEndDate(null);
            }
            // 处理工种转写
            level.setJobId(codeInfoService.translateCode(level.getJobId(), "hr_emp_skill_level", "SKILL_LEVEL_JOB_ID"));
        }
    }
}