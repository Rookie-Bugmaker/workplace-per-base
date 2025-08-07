package com.crrc.service.impl;

import com.crrc.entity.*;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.HrEmpHealthMapper;
import com.crrc.mapper.HrEmpHouseholdMapper;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.service.HrEmpBasicService;
import com.crrc.utils.SystemConstants;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工基本信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpBasicServiceImpl extends WorkServiceImpl<HrEmpBasicMapper, HrEmpBasic> implements HrEmpBasicService {

    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;

    @Autowired
    private HrEmpHouseholdMapper hrEmpHouseholdMapper;

    @Autowired
    private HrEmpHealthMapper hrEmpHealthMapper;

    public void savePlus(HrEmpBasicDTO hrEmpBasicDTO) {
        //基本信息中，健康状况，户口相关需要跨表保存

    }

    public HrEmpBasic selectByUserCode(String userCode) {
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>(HrEmpBasic.class)
                .selectAll(HrEmpBasic.class)
                .eq(HrEmpBasic::getUserCode, userCode);
        return hrEmpBasicMapper.selectOne(queryWrapper);
    }
    public List<HrEmpBasicDTO> selectMsg(String userCode){

        MPJLambdaWrapper<HrEmpBasic> queryWrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .selectAll(HrEmpBasic.class)
                .select(HrEmpCertificate::getCertiId, HrEmpCertificate::getCertiType)
                .leftJoin(HrEmpCertificate.class, HrEmpCertificate::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpHouseHold::getHouseHoldType, HrEmpHouseHold::getHouseHoldLocation, HrEmpHouseHold::getHouseHoldLocationDetail)
                .leftJoin(HrEmpHouseHold.class, HrEmpHouseHold::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpCommunication::getMobilePhone, HrEmpCommunication::getPersonalEmail, HrEmpCommunication::getAddress, HrEmpCommunication::getWorkEmail)
                .leftJoin(HrEmpCommunication.class, HrEmpCommunication::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpHealth::getHealthStatus)
                .leftJoin(HrEmpHealth.class, HrEmpHealth::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpProfessionalSkill::getSkillLevel, HrEmpProfessionalSkill::getSkillName)
                .leftJoin(HrEmpProfessionalSkill.class, HrEmpProfessionalSkill::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpEducation::getSchoolName, HrEmpEducation::getEducation, HrEmpEducation::getDegree, HrEmpEducation::getSchoolName, HrEmpEducation::getSchoolName)
                .leftJoin(HrEmpEducation.class, HrEmpEducation::getUserCode, HrEmpBasic::getUserCode)
                .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                .eq(HrEmpEducation::getIsHighestDegree, SystemConstants.TRUE_FLAG)
                .eq(HrEmpBasic::getUserCode, userCode);

        return hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, queryWrapper);
    }
}