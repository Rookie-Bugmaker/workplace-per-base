package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpCadreAppointmentInfo;

import java.util.List;

/**
 * <p>
 * 中干任职信息表 服务类
 * </p>
 *
 * @author yukun
 * @since 2026-05-19
 */
public interface HrEmpCadreAppointmentInfoService extends IService<HrEmpCadreAppointmentInfo> {

    /**
     * 根据档案号查询中干任职信息并翻译码值
     * @param userCode 档案号
     * @return 翻译后的任职信息列表
     */
    List<HrEmpCadreAppointmentInfo> getDetails(String userCode);

}
