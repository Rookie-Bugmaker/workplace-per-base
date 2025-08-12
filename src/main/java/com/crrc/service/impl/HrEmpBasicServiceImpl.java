package com.crrc.service.impl;

import com.crrc.entity.*;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.HrEmpEducationMapper;
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
    private HrEmpEducationMapper hrEmpEducationMapper;

    @Autowired
    private HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService;

    @Autowired
    private HrEmpMajorInfoServiceImpl hrEmpMajorInfoService;

//    public HrEmpBasic selectByUserCode(String userCode) {
//        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>(HrEmpBasic.class)
//                .selectAll(HrEmpBasic.class)
//                .eq(HrEmpBasic::getUserCode, userCode);
//        return hrEmpBasicMapper.selectOne(queryWrapper);
//    }
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
                .eq(HrEmpBasic::getUserCode, userCode);

        MPJLambdaWrapper<HrEmpEducation> queryWrapper2 = JoinWrappers.lambda(HrEmpEducation.class)
                .select(HrEmpEducation::getSchoolCode, HrEmpEducation::getEducation, HrEmpEducation::getDegree, HrEmpEducation::getIsHighestEducation, HrEmpEducation::getMajorCode)
                .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                .eq(HrEmpBasic::getUserCode, userCode);

        List<HrEmpBasicDTO> result = hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, queryWrapper);
        List<HrEmpEducation> hrEmpEducationList = hrEmpEducationMapper.selectList(queryWrapper2);
        if(hrEmpEducationList.size() > 1) {
            return result;
        }
        if(hrEmpEducationList.get(0) != null){
            HrEmpEducation hrEmpEducation = new HrEmpEducation();
            HrEmpBasicDTO hrEmpBasicDTO = result.get(0);
            HrEmpSchoolInfo hrEmpSchoolInfo = hrEmpSchoolInfoService.selectByCode(hrEmpEducation.getSchoolCode());
            if(hrEmpSchoolInfo != null) {
                hrEmpEducation.setSchoolName(hrEmpSchoolInfo.getSchoolName());
            }else {
                hrEmpEducation.setSchoolName("未知院校");
            }
            HrEmpMajorInfo hrEmpMajorInfo = hrEmpMajorInfoService.selectByCode(hrEmpEducation.getMajorCode());
            if(hrEmpMajorInfo != null) {
                hrEmpEducation.setMajor(hrEmpMajorInfo.getMajor());
            } else {
                hrEmpEducation.setMajor("未知专业");
            }
            hrEmpBasicDTO.setSchoolName(hrEmpEducation.getSchoolName());
            hrEmpBasicDTO.setEducation(hrEmpEducation.getEducation());
            hrEmpBasicDTO.setDegree(hrEmpEducation.getDegree());
            hrEmpBasicDTO.setMajor(hrEmpEducation.getMajor());
        }
        return result;
    }
}