package com.crrc.service.impl;

import com.crrc.entity.HrEmpAppointment;
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
                .eq(HrEmpOtherAppointment::getUserCode, userCode)
                .orderByAsc(HrEmpOtherAppointment::getStartDate);
        return hrEmpOtherAppointmentMapper.selectList(wrapper);
    }

    /**
     * 查询个人未结束的其它任职信息
     * @param userCode 员工档案号
     * @return 其它任职信息列表
     */
    public List<HrEmpOtherAppointment> selectUndoneApp(String userCode) {
        MPJLambdaWrapper<HrEmpOtherAppointment> wrapper = new MPJLambdaWrapper<HrEmpOtherAppointment>()
                .eq(HrEmpOtherAppointment::getUserCode, userCode)
                .and(w -> w
                        .isNull(HrEmpAppointment::getEndDate)
                        .or()
                        .gt(HrEmpAppointment::getEndDate, "2198-01-01")
                );
        List<HrEmpOtherAppointment> result = hrEmpOtherAppointmentMapper.selectList(wrapper);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }
}