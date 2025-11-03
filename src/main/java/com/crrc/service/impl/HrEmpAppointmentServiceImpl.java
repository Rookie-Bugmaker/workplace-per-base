package com.crrc.service.impl;

import com.crrc.entity.HrEmpAppointment;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.service.HrEmpAppointmentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 任职信息 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpAppointmentServiceImpl extends WorkServiceImpl<HrEmpAppointmentMapper, HrEmpAppointment> implements HrEmpAppointmentService {

    @Autowired
    private HrEmpAppointmentMapper hrEmpAppointmentMapper;

    public List<HrEmpAppointment> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .selectAll(HrEmpAppointment.class)
                .eq(HrEmpAppointment::getUserCode, userCode)
                .orderByAsc(HrEmpAppointment::getStartDate);
        return hrEmpAppointmentMapper.selectList(wrapper);
    }

    public HrEmpAppointment selectLatestMsg(String userCode){
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .selectAll(HrEmpAppointment.class)
                .eq(HrEmpAppointment::getUserCode, userCode)
                .orderByDesc(HrEmpAppointment::getStartDate);
        return hrEmpAppointmentMapper.selectList(wrapper).get(0);
    }

    /**
     * 查询个人未结束的任职信息
     * @param userCode 员工档案号
     * @return
     */
    public List<HrEmpAppointment> selectUndoneApp(String userCode) {
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .eq(HrEmpAppointment::getUserCode, userCode)
                .isNull(HrEmpAppointment::getEndDate);
        List<HrEmpAppointment> result = hrEmpAppointmentMapper.selectList(wrapper);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }
}