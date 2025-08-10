package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpProfessionalQualification;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.mapper.HrEmpProfessionalQualificationMapper;
import com.crrc.service.HrEmpProfessionalQualificationService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工专业职务任职资格表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpProfessionalQualificationServiceImpl extends WorkServiceImpl<HrEmpProfessionalQualificationMapper, HrEmpProfessionalQualification> implements HrEmpProfessionalQualificationService {

    @Autowired
    private HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper;

    public List<HrEmpProfessionalQualification> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpProfessionalQualification> wrapper = new MPJLambdaWrapper<HrEmpProfessionalQualification>()
                .selectAll(HrEmpProfessionalQualification.class)
                .eq(HrEmpProfessionalQualification::getUserCode, userCode);
        return hrEmpProfessionalQualificationMapper.selectList(wrapper);
    }
}