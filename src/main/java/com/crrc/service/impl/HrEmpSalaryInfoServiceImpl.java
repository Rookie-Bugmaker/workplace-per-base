package com.crrc.service.impl;

import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.mapper.HrEmpSalaryInfoMapper;
import com.crrc.service.HrEmpSalaryInfoService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工薪资信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-08-26 15:23:52
 */
@Service
public class HrEmpSalaryInfoServiceImpl extends WorkServiceImpl<HrEmpSalaryInfoMapper, HrEmpSalaryInfo> implements HrEmpSalaryInfoService {

    @Autowired
    private HrEmpSalaryInfoMapper hrEmpSalaryInfoMapper;

    public List<HrEmpSalaryInfo> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpSalaryInfo> wrapper = new MPJLambdaWrapper<HrEmpSalaryInfo>()
                .selectAll(HrEmpSalaryInfo.class)
                .eq(HrEmpSalaryInfo::getUserCode, userCode);
        return hrEmpSalaryInfoMapper.selectList(wrapper);
    }

}