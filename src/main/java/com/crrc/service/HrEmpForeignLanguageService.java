package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpForeignLanguage;

import java.util.List;

/**
 * <p>
 * 外语能力信息表 服务类
 * </p>
 *
 * @author system
 * @since 2026-04-16
 */
public interface HrEmpForeignLanguageService extends IService<HrEmpForeignLanguage> {

    List<HrEmpForeignLanguage> selectMsg(String userCode);
}
