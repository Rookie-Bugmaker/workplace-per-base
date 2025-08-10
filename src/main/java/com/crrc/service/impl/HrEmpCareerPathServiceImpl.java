package com.crrc.service.impl;

import com.crrc.entity.HrEmpCareerPath;
import com.crrc.entity.HrEmpEducation;
import com.crrc.mapper.HrEmpCareerPathMapper;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.service.HrEmpCareerPathService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 职业发展通道 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpCareerPathServiceImpl extends WorkServiceImpl<HrEmpCareerPathMapper, HrEmpCareerPath> implements HrEmpCareerPathService {
    @Autowired
    private HrEmpCareerPathMapper hrEmpCareerPathMapper;

    public List<HrEmpCareerPath> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpCareerPath> wrapper = new MPJLambdaWrapper<HrEmpCareerPath>()
                .selectAll(HrEmpCareerPath.class)
                .eq(HrEmpCareerPath::getUserCode, userCode);
        return hrEmpCareerPathMapper.selectList(wrapper);
    }

}