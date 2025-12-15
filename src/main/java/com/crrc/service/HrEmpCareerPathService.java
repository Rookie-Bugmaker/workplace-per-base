package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpCareerPath;

import java.util.List;

/**
 * <p>
 * 职业发展通道 服务类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
public interface HrEmpCareerPathService extends IService<HrEmpCareerPath> {

    /**
     * 翻译或处理HrEmpCareerPath的各个字段
     * @param hrEmpCareerPaths 需要处理的职业发展通道list
     */
     void resolveDetails(List<HrEmpCareerPath> hrEmpCareerPaths);
 }