package com.crrc.service.impl;

import com.crrc.client.PerRequireClient;
import com.crrc.common.ResultData;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.HrEmpForeignLanguage;
import com.crrc.mapper.HrEmpForeignLanguageMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.TreeVO;
import com.crrc.service.CodeInfoService;
import com.crrc.service.HrEmpForeignLanguageService;
import com.crrc.utils.FileUtils;
import com.crrc.utils.TreeUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 证书码值为"10"时允许填写手写名称
    private static final String CERT_CODE_ALLOW_WRITE = "10";

    @Autowired
    private HrEmpForeignLanguageMapper hrEmpForeignLanguageMapper;

    @Qualifier("com.crrc.client.PerRequireClient")
    @Autowired
    private PerRequireClient perRequireClient;

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
            hrEmpForeignLanguageList.forEach(item -> item.setFileHeaderId(item.getAttachment()));
            FileUtils.resolveFilesByHeaderId(hrEmpForeignLanguageList);
        } catch (Exception e) {
            log.warn("外语能力附件信息解析失败, {}", e.getMessage());
        }
    }

    /**
     * 翻译语种和证书名称的字典码为显示值
     */
    public void resolveLanguageAndCertValues(List<HrEmpForeignLanguage> hrEmpForeignLanguageList) {
        if (hrEmpForeignLanguageList == null || hrEmpForeignLanguageList.isEmpty()) {
            return;
        }

        ResponseResult<TreeVO<CodeInfo>> resultData = perRequireClient.getTreeById("00000000-0000-0000-0000-000000000001");
        Map<String, String> translateMap;
        if(resultData == null || resultData.getData() == null) {
            translateMap = new HashMap<>();
        } else {
            translateMap = TreeUtils.flattenDescendants(resultData.getData(), TreeVO::getChildren, true)
                    .stream().collect(Collectors.toMap(
                            k -> k.getNode().getCode(),
                            v -> v.getNode().getCodeValue(),
                            (a, b) -> a
                    ));
        }

        hrEmpForeignLanguageList.forEach(lang -> {
            lang.setLanguageValue(translateMap.getOrDefault(lang.getLanguage(), lang.getLanguage()));
            lang.setCertificateNameValue(translateMap.getOrDefault(lang.getCertificateName(), lang.getCertificateName()));
        });
    }

    /**
     * 校验证书手写内容：仅当证书名称码值为"10"时允许填写，否则清空
     */
    public void clearCertificateNameWriteIfNotApplicable(List<HrEmpForeignLanguage> hrEmpForeignLanguageList) {
        if (hrEmpForeignLanguageList == null || hrEmpForeignLanguageList.isEmpty()) {
            return;
        }
        hrEmpForeignLanguageList.forEach(lang -> {
            if (!CERT_CODE_ALLOW_WRITE.equals(lang.getCertificateName())) {
                lang.setCertificateNameWrite(null);
            }
        });
    }
}
