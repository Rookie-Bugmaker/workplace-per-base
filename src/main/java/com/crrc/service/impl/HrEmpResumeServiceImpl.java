package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.entity.HrEmpOtherAppointment;
import com.crrc.entity.HrEmpResume;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.mapper.HrEmpOtherAppointmentMapper;
import com.crrc.mapper.HrEmpResumeMapper;
import com.crrc.service.HrEmpResumeService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
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
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private HrEmpOtherAppointmentMapper hrEmpOtherAppointmentMapper;

    public List<HrEmpResume> selectMsg(String userCode) {
        MPJLambdaWrapper<HrEmpResume> wrapper = new MPJLambdaWrapper<HrEmpResume>()
                .selectAll(HrEmpResume.class)
                .eq(HrEmpResume::getUserCode, userCode)
                .orderByAsc(HrEmpResume::getStartDate);

        return hrEmpResumeMapper.selectList(wrapper);
    }

    @Override
    public void resolveDetails(List<HrEmpResume> hrEmpResumes) {
        for(HrEmpResume hrEmpResume : hrEmpResumes) {
            // 根据userCode，开始时间查询对应的appointment，以处理变动类型
            LambdaQueryWrapper<HrEmpAppointment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HrEmpAppointment::getUserCode, hrEmpResume.getUserCode());
            queryWrapper.eq(HrEmpAppointment::getStartDate, hrEmpResume.getStartDate());

            List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(queryWrapper);
            if (ObjectUtils.isEmpty(hrEmpAppointments)) {
                // 没有对应的appointment，无法处理变动类型
                log.info("没有对应的appointment，无法处理变动类型");
            } else {
                if (hrEmpAppointments.size() > 1) {
                    // 如果这种情况下，还有多条，不处理变动类型（无法一一对应）
                    log.error("注意！下面的履历有多条能对应的appointment: {}，无法处理变动类型", hrEmpResume.getId());
                }
                // 无论是否只有一条匹配结果，都会set变动类型
                hrEmpResume.setChangeType(hrEmpAppointments.get(0).getChangeType());
            }

            // 如果扫完了appointment还没找到，尝试查询otherAppointment中的其它任职类型
            if(StringUtils.isNotBlank(hrEmpResume.getChangeType())) {
                // 根据userCode，开始时间查询对应的otherAppointment，以处理变动类型
                LambdaQueryWrapper<HrEmpOtherAppointment> queryWrapperOther = new LambdaQueryWrapper<>();
                queryWrapperOther.eq(HrEmpOtherAppointment::getUserCode, hrEmpResume.getUserCode());
                queryWrapperOther.eq(HrEmpOtherAppointment::getStartDate, hrEmpResume.getStartDate());

                List<HrEmpOtherAppointment> hrEmpOtherAppointments = hrEmpOtherAppointmentMapper.selectList(queryWrapperOther);
                if (ObjectUtils.isEmpty(hrEmpOtherAppointments)) {
                    // 没有对应的appointment，无法处理变动类型
                    log.info("没有对应的otherAppointment，无法处理变动类型");
                } else {
                    if (hrEmpOtherAppointments.size() > 1) {
                        // 如果这种情况下，还有多条，不处理变动类型（无法一一对应）
                        log.error("注意！下面的履历有多条能对应的otherAppointment: {}，无法处理变动类型", hrEmpResume.getId());
                    }
                    // 无论是否只有一条匹配结果，都会set变动类型
                    hrEmpResume.setChangeType(hrEmpOtherAppointments.get(0).getOtherAppointmentType());
                }
            }
            // 如果关联上了变动类型，尝试进行反写
            if(StringUtils.isNotBlank(hrEmpResume.getChangeType())) {
                String newType = codeInfoService.translateCode(hrEmpResume.getChangeType(), "hr_emp_appointment","APPOINTMENT_CHANGE_TYPE");
                hrEmpResume.setChangeType(newType);
            }

            // 处理结束日期，如果结束日期在2198年之后的，置为空
            if(hrEmpResume.getEndDate() != null && hrEmpResume.getEndDate().isAfter(LocalDate.parse("2198-01-01"))) {
                hrEmpResume.setEndDate(null);
            }

            // 处理工作单位
            String originUnitCode = hrEmpResume.getUnit();
            if (StringUtils.isNotBlank(originUnitCode)) {
                String deptCode = originUnitCode.matches("^\\d.*") ?
                        codeInfoService.translateCode(originUnitCode, "hr_emp_appointment", "APPOINTMENT_ORG") : originUnitCode;
                hrEmpResume.setUnit(deptCode);
            }

            // 处理工作部门
            String originDeptCode = hrEmpResume.getDepartment();
            if (StringUtils.isNotBlank(originDeptCode)) {
                String deptCode = originDeptCode.matches("^\\d.*") ?
                        codeInfoService.translateCode(originDeptCode, "hr_emp_appointment", "APPOINTMENT_ORG") : originDeptCode;
                hrEmpResume.setDepartment(deptCode);
            }


        }
    }
}