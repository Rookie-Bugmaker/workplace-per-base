package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.entity.HrEmpResume;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.mapper.HrEmpResumeMapper;
import com.crrc.service.HrEmpResumeService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * <p>
 * 员工履历信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
@Slf4j
public class HrEmpResumeServiceImpl extends WorkServiceImpl<HrEmpResumeMapper, HrEmpResume> implements HrEmpResumeService {

    @Autowired
    private HrEmpResumeMapper hrEmpResumeMapper;
    @Autowired
    private HrEmpAppointmentMapper hrEmpAppointmentMapper;

    public List<HrEmpResume> selectMsg(String userCode) {
        MPJLambdaWrapper<HrEmpResume> wrapper = new MPJLambdaWrapper<HrEmpResume>()
                .selectAll(HrEmpResume.class)
                .eq(HrEmpResume::getUserCode, userCode)
                .orderByAsc(HrEmpResume::getStartDate);
        LambdaQueryWrapper<HrEmpAppointment> queryWrapper = new LambdaQueryWrapper<>();
        List<HrEmpResume> hrEmpResumes = hrEmpResumeMapper.selectList(wrapper);
        for(HrEmpResume hrEmpResume : hrEmpResumes) {
            // 根据userCode，开始时间和结束时间查询对应的appointment，以处理变动类型
            queryWrapper.eq(HrEmpAppointment::getUserCode, hrEmpResume.getUserCode());
            queryWrapper.eq(HrEmpAppointment::getStartDate, hrEmpResume.getStartDate());
            if(hrEmpResume.getEndDate() == null) {
                queryWrapper.isNull(HrEmpAppointment::getEndDate);
            } else {
                queryWrapper.eq(HrEmpAppointment::getEndDate, hrEmpResume.getEndDate());
            }
            List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(queryWrapper);
            if(ObjectUtils.isEmpty(hrEmpAppointments)) {
                // 没有对应的appointment，无法处理变动类型
                log.info("没有对应的appointment，无法处理变动类型");
            } else if(hrEmpAppointments.size() > 1) {
                // 如果这种情况下，还有多条，不处理变动类型（无法一一对应）
                log.error("注意！下面的履历有多条能对应的appointment: {}，无法处理变动类型", hrEmpResume.getId());
            } else {
                hrEmpResume.setChangeType(hrEmpAppointments.get(0).getChangeType());
            }

        }

        return hrEmpResumes;
    }

}