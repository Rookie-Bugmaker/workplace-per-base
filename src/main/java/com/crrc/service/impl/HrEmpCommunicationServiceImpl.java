package com.crrc.service.impl;

import com.crrc.entity.HrEmpCommunication;
import com.crrc.mapper.HrEmpCommunicationMapper;
import com.crrc.service.HrEmpCommunicationService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工通讯信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpCommunicationServiceImpl extends WorkServiceImpl<HrEmpCommunicationMapper, HrEmpCommunication> implements HrEmpCommunicationService {

    @Autowired
    private HrEmpCommunicationMapper hrEmpCommunicationMapper;

    public HrEmpCommunication selectByUserCode(String userCode) {
        MPJLambdaWrapper<HrEmpCommunication> queryWrapper = new MPJLambdaWrapper<>(HrEmpCommunication.class)
                .selectAll(HrEmpCommunication.class)
                .eq(HrEmpCommunication::getUserCode, userCode);
        return hrEmpCommunicationMapper.selectOne(queryWrapper);
    }
}