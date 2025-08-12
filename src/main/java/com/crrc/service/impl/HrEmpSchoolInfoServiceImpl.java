package com.crrc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpSchoolInfo;
import com.crrc.mapper.HrEmpSchoolInfoMapper;
import com.crrc.service.HrEmpSchoolInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HrEmpSchoolInfoServiceImpl extends WorkServiceImpl<HrEmpSchoolInfoMapper, HrEmpSchoolInfo> implements HrEmpSchoolInfoService {

    @Autowired
    private HrEmpSchoolInfoMapper hrEmpSchoolInfoMapper;

    /**
     *  通过学校编码查询实体
     * @param schoolCode 学校编码
     * @return 正常返回查询到的实体，如果实体有异常，返回空
     */
    public HrEmpSchoolInfo selectByCode(String schoolCode){
        HrEmpSchoolInfo schoolInfo = hrEmpSchoolInfoMapper.selectOne(new LambdaQueryWrapper<HrEmpSchoolInfo>().
                select(HrEmpSchoolInfo::getSchoolCode, HrEmpSchoolInfo::getSchoolName)
                .eq(HrEmpSchoolInfo::getSchoolCode, schoolCode));
        if(schoolInfo == null || schoolInfo.getSchoolCode() == null || schoolInfo.getSchoolName() == null){
            return null;
        } else {
            return schoolInfo;
        }
    }
}
