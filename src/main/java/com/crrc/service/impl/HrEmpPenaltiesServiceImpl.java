package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPenalties;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.mapper.HrEmpPenaltiesMapper;
import com.crrc.service.HrEmpPenaltiesService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工惩处信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpPenaltiesServiceImpl extends WorkServiceImpl<HrEmpPenaltiesMapper, HrEmpPenalties> implements HrEmpPenaltiesService {

    @Autowired
    private HrEmpPenaltiesMapper hrEmpPenaltiesMapper;

    public List<HrEmpPenalties> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpPenalties> wrapper = new MPJLambdaWrapper<HrEmpPenalties>()
                .selectAll(HrEmpPenalties.class)
                .eq(HrEmpPenalties::getUserCode, userCode);
        return hrEmpPenaltiesMapper.selectList(wrapper);
    }
}