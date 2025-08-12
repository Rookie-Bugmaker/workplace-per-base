package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpMajorInfo;
import com.crrc.mapper.HrEmpMajorInfoMapper;
import com.crrc.service.HrEmpMajorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HrEmpMajorInfoServiceImpl extends WorkServiceImpl<HrEmpMajorInfoMapper, HrEmpMajorInfo> implements HrEmpMajorInfoService {

    @Autowired
    private HrEmpMajorInfoMapper hrEmpMajorInfoMapper;

    /**
     *  通过专业编码查询实体
     * @param majorCode 专业编码
     * @return 正常返回查询到的实体，如果实体有异常，返回空
     */
    public HrEmpMajorInfo selectByCode(String majorCode){
        HrEmpMajorInfo hrEmpMajorInfo = hrEmpMajorInfoMapper.selectOne(new LambdaQueryWrapper<HrEmpMajorInfo>().
                select(HrEmpMajorInfo::getMajorCode, HrEmpMajorInfo::getMajor)
                .eq(HrEmpMajorInfo::getMajorCode, majorCode));
        if(hrEmpMajorInfo == null || hrEmpMajorInfo.getMajor() == null || hrEmpMajorInfo.getMajorCode() == null){
            return null;
        } else {
            return hrEmpMajorInfo;
        }
    }
}
