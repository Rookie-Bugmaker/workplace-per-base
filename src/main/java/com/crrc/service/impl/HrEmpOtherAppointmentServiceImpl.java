package com.crrc.service.impl;

import com.crrc.entity.HrEmpOtherAppointment;
import com.crrc.mapper.HrEmpOtherAppointmentMapper;
import com.crrc.service.HrEmpOtherAppointmentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 其他任职信息 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpOtherAppointmentServiceImpl extends WorkServiceImpl<HrEmpOtherAppointmentMapper, HrEmpOtherAppointment> implements HrEmpOtherAppointmentService {

    @Autowired
    private HrEmpOtherAppointmentMapper hrEmpOtherAppointmentMapper;

    public List<HrEmpOtherAppointment> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpOtherAppointment> wrapper = new MPJLambdaWrapper<HrEmpOtherAppointment>()
                .selectAll(HrEmpOtherAppointment.class)
                .eq(HrEmpOtherAppointment::getUserCode, userCode);
        return hrEmpOtherAppointmentMapper.selectList(wrapper);
    }
}