package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.mapper.HrEmpEducationMapper;
import com.crrc.service.HrEmpEducationService;
import com.crrc.utils.FileUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class HrEmpEducationServiceImpl extends WorkServiceImpl<HrEmpEducationMapper, HrEmpEducation> implements HrEmpEducationService {

    @Autowired
    private HrEmpEducationMapper hrEmpEducationMapper;

    @Override
    public List<HrEmpEducation> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpEducation> wrapper = new MPJLambdaWrapper<HrEmpEducation>()
                .selectAll(HrEmpEducation.class)
                .eq(HrEmpEducation::getUserCode, userCode)
                .orderByAsc(HrEmpEducation::getGraduationDate);
        return hrEmpEducationMapper.selectList(wrapper);
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(List<HrEmpEducation> hrEmpEducations) {
        try {
            FileUtils.resolveFilesByHeaderId(hrEmpEducations);
        } catch (Exception e) {
            log.warn("人员附件信息解析失败, {}", e.getMessage());
        }
    }

}