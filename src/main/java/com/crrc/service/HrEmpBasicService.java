package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpBasic;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrFullDataDTO;

import java.util.List;

/**
 * <p>
 * 员工基本信息表 服务类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
public interface HrEmpBasicService extends IService<HrEmpBasic> {

    List<HrEmpBasicDTO> selectMsg(String userCode);

    /**
     * 根据用户名批量查询全量个人信息（最新一条）
     * @param userCodes 需要查询的用户名列表
     * @return 个人最新一条信息
     */
    List<HrFullDataDTO> getFullData(List<String> userCodes);
}