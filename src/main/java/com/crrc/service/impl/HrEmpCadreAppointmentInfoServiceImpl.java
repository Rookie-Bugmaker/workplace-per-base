package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpCadreAppointmentInfo;
import com.crrc.entity.PositionInfo;
import com.crrc.mapper.HrEmpCadreAppointmentInfoMapper;
import com.crrc.service.HrEmpCadreAppointmentInfoService;
import com.crrc.utils.DeptUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 中干任职信息表 服务实现类
 * </p>
 *
 * @author yukun
 * @since 2026-05-19
 */
@Service
public class HrEmpCadreAppointmentInfoServiceImpl extends WorkServiceImpl<HrEmpCadreAppointmentInfoMapper, HrEmpCadreAppointmentInfo> implements HrEmpCadreAppointmentInfoService {

    private final PositionInfoServiceImpl positionInfoService;

    public HrEmpCadreAppointmentInfoServiceImpl(PositionInfoServiceImpl positionInfoService) {
        this.positionInfoService = positionInfoService;
    }

    @Override
    public List<HrEmpCadreAppointmentInfo> getDetails(String userCode) {
        LambdaQueryWrapper<HrEmpCadreAppointmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpCadreAppointmentInfo::getUserCode, userCode);
        List<HrEmpCadreAppointmentInfo> list = this.baseMapper.selectList(wrapper);
        Set<String> positionCodeList = list.stream().map(HrEmpCadreAppointmentInfo::getPosition).collect(Collectors.toSet());
        if(positionCodeList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> positionMap = positionInfoService.lambdaQuery().in(PositionInfo::getCode, positionCodeList)
                .list().stream().collect(Collectors.toMap(
                        PositionInfo::getCode,
                        PositionInfo::getName,
                        (a, b) -> a
                ));

        Map<String, String> deptTranslateMap = DeptUtils.getDeptTranslateMap();

        for (HrEmpCadreAppointmentInfo entity : list) {
            // 翻译职位
            String position = entity.getPosition();
            if (StringUtils.isNotBlank(position)) {
                entity.setPositionValue(
                        positionMap.getOrDefault(position, position)
                );
            }

            // 翻译部门
            String dept = entity.getDept();
            if (StringUtils.isNotBlank(dept)) {
                entity.setDeptValue(deptTranslateMap.getOrDefault(dept, dept));
            }

            // 翻译组织
            String org = entity.getOrg();
            if (StringUtils.isNotBlank(org)) {
                entity.setOrgValue(deptTranslateMap.getOrDefault(org, org));
            }

            // 翻译组织单元
            String orgUnit = entity.getOrgUnit();
            if (StringUtils.isNotBlank(orgUnit)) {
                entity.setOrgUnitValue(deptTranslateMap.getOrDefault(orgUnit, orgUnit));
            }

            // 翻译试用期状态
            String probationStatus = entity.getProbationStatus();
            if (StringUtils.isNotBlank(probationStatus)) {
                entity.setProbationStatusValue("0".equals(probationStatus) ? "试用期" : "任期");
            }

            String isMajorPosition = entity.getIsMajorPosition();
            if (StringUtils.isNotBlank(isMajorPosition)) {
                entity.setIsMajorPositionValue("0".equals(isMajorPosition) ? "兼职" : "主职");
            }
        }
        return list;
    }

}
