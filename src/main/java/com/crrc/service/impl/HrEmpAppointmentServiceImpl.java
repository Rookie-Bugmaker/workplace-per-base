package com.crrc.service.impl;

import com.crrc.entity.HrEmpAppointment;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.service.HrEmpAppointmentService;
import com.crrc.utils.DeptUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    public List<HrEmpAppointment> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .selectAll(HrEmpAppointment.class)
                .eq(HrEmpAppointment::getUserCode, userCode)
                .last("ORDER BY create_time::DATE ASC NULLS FIRST, sort_no ASC NULLS FIRST, " +
                        "start_date ASC NULLS FIRST");
        return hrEmpAppointmentMapper.selectList(wrapper);
    }

    public List<HrEmpAppointment> selectLatestMsg(List<String> userCodeList){

        if (userCodeList == null || userCodeList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = userCodeList.stream()
                .filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());

        MPJLambdaWrapper<HrEmpAppointment> w = new MPJLambdaWrapper<HrEmpAppointment>()
                // 关键：distinct on 每个 user_code 只保留一行
                .select("distinct on (t.user_code) t.*")
                .in(HrEmpAppointment::getUserCode, codes)
                // order by 顺序必须以 distinct on 的字段开头
                .orderByAsc("t.user_code")
                // Wrapper 不支持 NULLS LAST，用布尔表达式把 NULL 放到最后（false<true）
                .orderByAsc("(t.start_date is null)")
                .orderByDesc("t.start_date")
                .orderByDesc("t.id");  // 并列时间时用主键稳定排序
        List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(w);
        // 回显码值，当数据为数字开头的时候才会进行转写，否则直接返回
        for(HrEmpAppointment entity : hrEmpAppointments) {
            String deptCode = entity.getDept();
            String deptName = DeptUtils.getDeptNameByCode(deptCode);
            entity.setDept(deptName);

            // 处理组织单元
            String orgId = entity.getOrgUnit();
            String orgName = DeptUtils.getDeptNameByCode(orgId);
            entity.setOrgUnit(orgName);

            // 处理组织
            String orgOriginal = entity.getOrg();
            if (StringUtils.isNotBlank(orgOriginal)) {
                String org = orgOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(orgOriginal, "hr_emp_appointment", "APPOINTMENT_ORG") : orgOriginal;
                entity.setOrg(org);
            }

            // 处理职位名称
            String position = entity.getPosition();
            if (StringUtils.isNotBlank(position)) {
                String positionName = position.matches("^\\d.*") ?
                        codeInfoService.translateCode(position, "hr_emp_appointment", "APPOINTMENT_POSITION") : position;
                entity.setPosition(positionName);
            }

            // 处理变动事件 changeEvent
            String changeEventOriginal = entity.getChangeEvent();
            if (StringUtils.isNotBlank(changeEventOriginal)) {
                String changeEvent = changeEventOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeEventOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_EVENT") : changeEventOriginal;
                entity.setChangeEvent(changeEvent);
            }

            // 处理变动类型 changeType
            String changeTypeOriginal = entity.getChangeType();
            if (StringUtils.isNotBlank(changeTypeOriginal)) {
                String changeType = changeTypeOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeTypeOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE") : changeTypeOriginal;
                entity.setChangeType(changeType);
            }

            // 处理变动原因 changeReason
            String changeReasonOriginal = entity.getChangeReason();
            if (StringUtils.isNotBlank(changeReasonOriginal)) {
                String changeReason = changeReasonOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeReasonOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_REASON") : changeReasonOriginal;
                entity.setChangeReason(changeReason);
            }

            // 处理在岗状态 dutyStatus
            String dutyStatusOriginal = entity.getDutyStatus();
            if (StringUtils.isNotBlank(dutyStatusOriginal)) {
                String dutyStatus = dutyStatusOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(dutyStatusOriginal, "hr_emp_appointment", "APPOINTMENT_DUTY_STATUS") : dutyStatusOriginal;
                entity.setDutyStatus(dutyStatus);
            }

            // 处理职务类别 positionCategory
            String positionCategoryOriginal = entity.getPositionCategory();
            if (StringUtils.isNotBlank(positionCategoryOriginal)) {
                String positionCategory = positionCategoryOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(positionCategoryOriginal, "hr_emp_appointment", "APPOINTMENT_POSITION_CATEGORY") : positionCategoryOriginal;
                entity.setPositionCategory(positionCategory);
            }

        }
        return hrEmpAppointments;
    }

    /**
     * 查询个人未结束的任职信息
     * @param userCode 员工档案号
     * @return
     */
    public List<HrEmpAppointment> selectUndoneApp(String userCode) {
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .eq(HrEmpAppointment::getUserCode, userCode)
                .and(w -> w
                    .isNull(HrEmpAppointment::getEndDate)
                    .or()
                    .gt(HrEmpAppointment::getEndDate, "2198-01-01")
                );
        List<HrEmpAppointment> result = hrEmpAppointmentMapper.selectList(wrapper);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }
}