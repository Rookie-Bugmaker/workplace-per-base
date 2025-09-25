package com.crrc.service.impl;

import com.crrc.entity.HrEmpCertificate;
import com.crrc.mapper.HrEmpCertificateMapper;
import com.crrc.service.HrEmpCertificateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工证件信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpCertificateServiceImpl extends WorkServiceImpl<HrEmpCertificateMapper, HrEmpCertificate> implements HrEmpCertificateService {

    @Autowired
    private HrEmpCertificateMapper hrEmpCertificateMapper;

    public HrEmpCertificate selectByUserCode(String userCode) {
        MPJLambdaWrapper<HrEmpCertificate> queryWrapper = new MPJLambdaWrapper<>(HrEmpCertificate.class)
                .selectAll(HrEmpCertificate.class)
                .eq(HrEmpCertificate::getUserCode, userCode);
        return hrEmpCertificateMapper.selectOne(queryWrapper);
    }
}