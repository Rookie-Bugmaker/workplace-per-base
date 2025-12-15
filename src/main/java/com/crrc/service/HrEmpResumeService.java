package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpResume;

import java.util.List;

/**
 * <p>
 * 员工履历信息表 服务类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
public interface HrEmpResumeService extends IService<HrEmpResume> {

    /**
     * 处理员工履历信息的各个字段
     * @param hrEmpResumes 需要处理的员工履历信息
     */
     void resolveDetails(List<HrEmpResume> hrEmpResumes);
 }