package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpEducation;

import java.util.List;

/**
 * <p>
 * 员工教育信息表 服务类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
public interface HrEmpEducationService extends IService<HrEmpEducation> {
 List<HrEmpEducation> selectMsg(String userCode);
}