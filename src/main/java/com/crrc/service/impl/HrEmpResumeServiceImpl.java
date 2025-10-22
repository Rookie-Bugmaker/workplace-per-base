package com.crrc.service.impl;

import com.crrc.entity.HrEmpResume;
import com.crrc.mapper.HrEmpResumeMapper;
import com.crrc.service.HrEmpResumeService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class HrEmpResumeServiceImpl extends WorkServiceImpl<HrEmpResumeMapper, HrEmpResume> implements HrEmpResumeService {

    @Autowired
    private HrEmpResumeMapper hrEmpResumeMapper;

    public List<HrEmpResume> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpResume> wrapper = new MPJLambdaWrapper<HrEmpResume>()
                .selectAll(HrEmpResume.class)
                .eq(HrEmpResume::getUserCode, userCode)
                .orderByAsc(HrEmpResume::getStartDate);
        return hrEmpResumeMapper.selectList(wrapper);
    }

}