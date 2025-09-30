package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.service.HrEmpEducationService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工教育信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpEducationServiceImpl extends WorkServiceImpl<HrEmpEducationMapper, HrEmpEducation> implements HrEmpEducationService {

    @Autowired
    private HrEmpEducationMapper hrEmpEducationMapper;

    @Override
    public List<HrEmpEducation> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpEducation> wrapper = new MPJLambdaWrapper<HrEmpEducation>()
                .selectAll(HrEmpEducation.class)
                .eq(HrEmpEducation::getUserCode, userCode);
        return hrEmpEducationMapper.selectList(wrapper);
    }


}