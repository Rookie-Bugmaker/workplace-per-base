package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpSkillLevel;

import java.util.List;

/**
 * <p>
 * 职业技能等级 服务类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
public interface HrEmpSkillLevelService extends IService<HrEmpSkillLevel> {

    /**
     * 翻译或处理职业技能等级各个字段
     * @param hrEmpSkillLevels 需要处理的list
     */
     void resolveDetails(List<HrEmpSkillLevel> hrEmpSkillLevels);
 }