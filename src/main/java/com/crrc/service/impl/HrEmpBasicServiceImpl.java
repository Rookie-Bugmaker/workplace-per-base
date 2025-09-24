package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crrc.entity.*;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.service.HrEmpBasicService;
import com.crrc.utils.SystemConstants;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
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



    /**
     * 条件查询
     * 无参数返回全部数据
     * @param page 空白page，在controller层设置好size和pageNo
     * @param dto 查询实体
     * @return 分页list
     * */
    public IPage<HrEmpBasicDTO> listData(IPage<HrEmpBasicDTO> page, HrEmpBasicSearchDTO dto) {
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .distinct()
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
                .select(HrEmpEducation::getSchoolCode, HrEmpEducation::getEducation, HrEmpEducation::getDegree, HrEmpEducation::getIsHighestEducation, HrEmpEducation::getMajorCode)
                .leftJoin(HrEmpEducation.class,
                        on -> on.eq(HrEmpEducation::getUserCode, HrEmpBasic::getUserCode)
                                .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                )
                .leftJoin(HrEmpAppointment.class, HrEmpAppointment::getUserCode, HrEmpBasic::getUserCode);

        buildFuzzyConditions(queryWrapper, dto);
        queryWrapper.orderByAsc(HrEmpBasic::getUserCode);
        return hrEmpBasicMapper.selectJoinPage(page, HrEmpBasicDTO.class, queryWrapper);
    }


    private void buildFuzzyConditions(MPJLambdaWrapper<HrEmpBasic> wrapper, HrEmpBasicSearchDTO dto) {
        // 链式构建多个模糊查询条件
        wrapper
                .like(StringUtils.isNotBlank(dto.getName()),
                        HrEmpBasic::getName,
                        dto.getName())
                .like(StringUtils.isNotBlank(dto.getUserCode()),
                        HrEmpBasic::getUserCode,
                        dto.getUserCode())
                .like(StringUtils.isNotBlank(dto.getDept()),
                        HrEmpBasic::getDept,
                        dto.getDept())
                .like(StringUtils.isNotBlank(dto.getPosition()),
                        HrEmpAppointment::getPosition,
                        dto.getPosition());
    }


    /**
     *  通过档案号联表查询基本信息
     * @param userCode 档案号
     * @return 查询结果List
     */
    @Override
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
        if(!hrEmpEducationList.isEmpty()){
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