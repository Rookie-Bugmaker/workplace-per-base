package com.crrc.service.impl;

import com.crrc.entity.HrEmpForeignLanguage;
import com.crrc.mapper.HrEmpForeignLanguageMapper;
import com.crrc.service.HrEmpForeignLanguageService;
import com.crrc.utils.FileUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 外语能力信息表 服务实现类
 * </p>
 *
 * @author system
 * @since 2026-04-16
 */
@Slf4j
@Service
public class HrEmpForeignLanguageServiceImpl extends WorkServiceImpl<HrEmpForeignLanguageMapper, HrEmpForeignLanguage> implements HrEmpForeignLanguageService {

    @Autowired
    private HrEmpForeignLanguageMapper hrEmpForeignLanguageMapper;

    @Override
    public List<HrEmpForeignLanguage> selectMsg(String userCode) {
        MPJLambdaWrapper<HrEmpForeignLanguage> wrapper = new MPJLambdaWrapper<HrEmpForeignLanguage>()
                .selectAll(HrEmpForeignLanguage.class)
                .eq(HrEmpForeignLanguage::getUserCode, userCode)
                .orderByAsc(HrEmpForeignLanguage::getExamDate);
        return hrEmpForeignLanguageMapper.selectList(wrapper);
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(List<HrEmpForeignLanguage> hrEmpForeignLanguageList) {
        try {
            FileUtils.resolveFilesByHeaderId(hrEmpForeignLanguageList);
        } catch (Exception e) {
            log.warn("外语能力附件信息解析失败, {}", e.getMessage());
        }
    }
}
