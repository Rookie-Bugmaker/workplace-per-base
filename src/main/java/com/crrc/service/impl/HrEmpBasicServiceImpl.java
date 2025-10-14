package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crrc.api.WkCoreAPi;
import com.crrc.entity.*;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.mapper.HrEmpProfessionalQualificationMapper;
import com.crrc.mapper.HrEmpSkillLevelMapper;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.service.HrEmpBasicService;
import com.crrc.utils.SystemConstants;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
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
@Slf4j
public class HrEmpBasicServiceImpl extends WorkServiceImpl<HrEmpBasicMapper, HrEmpBasic> implements HrEmpBasicService {

    private final HrEmpBasicMapper hrEmpBasicMapper;

    private final HrEmpEducationMapper hrEmpEducationMapper;

    private final HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService;

    private final HrEmpMajorInfoServiceImpl hrEmpMajorInfoService;

    private final HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper;

    private final HrEmpSkillLevelMapper hrEmpSkillLevelMapper;

    private final WkCoreAPi wkCoreApi;

    public HrEmpBasicServiceImpl(HrEmpBasicMapper hrEmpBasicMapper, HrEmpEducationMapper hrEmpEducationMapper, HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService, HrEmpMajorInfoServiceImpl hrEmpMajorInfoService, HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper, HrEmpSkillLevelMapper hrEmpSkillLevelMapper, WkCoreAPi wkCoreApi) {
        this.hrEmpBasicMapper = hrEmpBasicMapper;
        this.hrEmpEducationMapper = hrEmpEducationMapper;
        this.hrEmpSchoolInfoService = hrEmpSchoolInfoService;
        this.hrEmpMajorInfoService = hrEmpMajorInfoService;
        this.hrEmpProfessionalQualificationMapper = hrEmpProfessionalQualificationMapper;
        this.hrEmpSkillLevelMapper = hrEmpSkillLevelMapper;
        this.wkCoreApi = wkCoreApi;
    }


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
     * 通过档案号查询员工完整信息（含关联表数据）
     * @param userCode 员工档案号
     * @return 员工基本信息DTO集合（通常仅1条记录）
     */
    @Override
    public List<HrEmpBasicDTO> selectMsg(String userCode) {
        // 1. 主表及左连接表查询
        List<HrEmpBasicDTO> result = queryBaseInfoWithJoins(userCode);
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 部门名称转换
        resolveDepartmentName(result.get(0));

        // 3. 查询关联子表数据
        SubTableData subData = querySubTableData(userCode);

        // 4. 处理子表数据（当所有子表数据唯一时）
        if (subData.isAllUnique()) {
            mergeSubTableData(result.get(0), subData);
        }

        // 5. 处理年龄
        resolveAge(result.get(0));

        return result;
    }


    /**
     * 主表关联查询（基础信息+左连接表）
     */
    private List<HrEmpBasicDTO> queryBaseInfoWithJoins(String userCode) {
        MPJLambdaWrapper<HrEmpBasic> wrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .selectAll(HrEmpBasic.class)
                // 证书信息
                .select(HrEmpCertificate::getCertiId, HrEmpCertificate::getCertiType)
                .leftJoin(HrEmpCertificate.class, HrEmpCertificate::getUserCode, HrEmpBasic::getUserCode)
                // 户籍信息
                .select(HrEmpHouseHold::getHouseHoldType, HrEmpHouseHold::getHouseHoldLocation, HrEmpHouseHold::getHouseHoldLocationDetail)
                .leftJoin(HrEmpHouseHold.class, HrEmpHouseHold::getUserCode, HrEmpBasic::getUserCode)
                // 联系信息
                .select(HrEmpCommunication::getMobilePhone, HrEmpCommunication::getPersonalEmail, HrEmpCommunication::getAddress, HrEmpCommunication::getWorkEmail)
                .leftJoin(HrEmpCommunication.class, HrEmpCommunication::getUserCode, HrEmpBasic::getUserCode)
                // 健康信息
                .select(HrEmpHealth::getHealthStatus)
                .leftJoin(HrEmpHealth.class, HrEmpHealth::getUserCode, HrEmpBasic::getUserCode)
                .eq(HrEmpBasic::getUserCode, userCode);

        return hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, wrapper);
    }

    /**
     * 解析部门名称（通过组织编码）
     */
    private void resolveDepartmentName(HrEmpBasicDTO dto) {
        if (StringUtils.isBlank(dto.getDept())) {
            return;
        }

        try {
            String deptName = wkCoreApi.getDeptByOrgCode(dto.getDept()).getDepartmentName();
            dto.setDept(deptName);
        } catch (Exception e) {
            log.warn("部门名称查询失败, orgCode: {}", dto.getDept(), e);
        }
    }

    /**
     * 计算并设置员工年龄（基于出生日期计算周岁）
     * @param dto 包含出生日期的员工DTO对象
     */
    private void resolveAge(HrEmpBasicDTO dto) {
        if (dto.getBirthDate() == null) {
            log.warn("档案号：{}，人员出生日期为空", dto.getUserCode());
            dto.setAge(null);
            return;
        }

        try {
            int age = Period.between(dto.getBirthDate(), LocalDate.now()).getYears();
            dto.setAge(Math.max(age, 0));
        } catch (Exception e) {
            log.error("年龄计算异常，档案号：{}", dto.getUserCode(), e);
            dto.setAge(null);
        }
    }

    /**
     * 查询子表数据（教育/技能/资格）
     */
    private SubTableData querySubTableData(String userCode) {
        SubTableData data = new SubTableData();

        // 教育信息（最高学历）
        data.educationList = hrEmpEducationMapper.selectList(
                JoinWrappers.lambda(HrEmpEducation.class)
                        .select(HrEmpEducation::getSchoolCode, HrEmpEducation::getEducation,
                                HrEmpEducation::getDegree, HrEmpEducation::getMajorCode)
                        .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpEducation::getUserCode, userCode)
        );

        // 技能等级（最高等级）
        data.skillLevelList = hrEmpSkillLevelMapper.selectList(
                JoinWrappers.lambda(HrEmpSkillLevel.class)
                        .select(HrEmpSkillLevel::getSkillLevel)
                        .eq(HrEmpSkillLevel::getIsHighest, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpSkillLevel::getUserCode, userCode)
        );

        // 职业资格（最高资格）
        data.qualificationList = hrEmpProfessionalQualificationMapper.selectList(
                JoinWrappers.lambda(HrEmpProfessionalQualification.class)
                        .select(HrEmpProfessionalQualification::getQuaId)
                        .eq(HrEmpProfessionalQualification::getIsHighest, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpProfessionalQualification::getUserCode, userCode)
        );

        return data;
    }

    /**
     * 合并子表数据到主DTO
     */
    private void mergeSubTableData(HrEmpBasicDTO dto, SubTableData data) {
        // 处理教育信息
        if (!data.educationList.isEmpty()) {
            HrEmpEducation edu = data.educationList.get(0);
            // 补充院校/专业名称
            enrichEducationInfo(edu);
            dto.setSchoolName(edu.getSchoolName());
            dto.setEducation(edu.getEducation());
            dto.setDegree(edu.getDegree());
            dto.setMajor(edu.getMajor());
        }

        // 处理技能等级
        if (!data.skillLevelList.isEmpty()) {
            dto.setSkillLevel(data.skillLevelList.get(0).getSkillLevel());
        }

        // 处理职业资格
        if (!data.qualificationList.isEmpty()) {
            dto.setQuaId(data.qualificationList.get(0).getQuaId());
        }
    }

    /**
     * 补充教育明细信息（院校名称+专业名称）
     */
    private void enrichEducationInfo(HrEmpEducation edu) {
        // 院校名称
        HrEmpSchoolInfo school = hrEmpSchoolInfoService.selectByCode(edu.getSchoolCode());
        edu.setSchoolName(school != null ? school.getSchoolName() : "未知院校");

        // 专业名称
        HrEmpMajorInfo major = hrEmpMajorInfoService.selectByCode(edu.getMajorCode());
        edu.setMajor(major != null ? major.getMajor() : "未知专业");
    }

    /**
     * 子表数据容器（内部使用）
     */
    private static class SubTableData {
        List<HrEmpEducation> educationList;
        List<HrEmpSkillLevel> skillLevelList;
        List<HrEmpProfessionalQualification> qualificationList;

        boolean isAllUnique() {
            return educationList.size() <= 1 &&
                    skillLevelList.size() <= 1 &&
                    qualificationList.size() <= 1;
        }
    }
}