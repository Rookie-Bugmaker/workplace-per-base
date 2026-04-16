package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpForeignLanguage;
import com.crrc.service.impl.HrEmpForeignLanguageServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 外语能力信息 Excel 导入监听器
 *
 * @author system
 * @since 2026-04-16
 */
public class ForeignLanguageImportListener implements ReadListener<HrEmpForeignLanguage> {

    private final HrEmpForeignLanguageServiceImpl hrEmpForeignLanguageService;

    private final List<HrEmpForeignLanguage> buffer = new ArrayList<>();

    public ForeignLanguageImportListener(HrEmpForeignLanguageServiceImpl hrEmpForeignLanguageService) {
        this.hrEmpForeignLanguageService = hrEmpForeignLanguageService;
    }

    @Override
    public void invoke(HrEmpForeignLanguage hrEmpForeignLanguage, AnalysisContext analysisContext) {
        // 必填字段校验
        hrEmpForeignLanguage.setId(IdUtil.randomUUID());
        if (!StringUtils.hasText(hrEmpForeignLanguage.getUserCode())) {
            throw new IllegalArgumentException("档案号必填");
        }

        buffer.add(hrEmpForeignLanguage);
        if (buffer.size() > 1000) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    private void flush() {
        // 校验证书手写内容：仅当证书名称码值为"10"时允许填写
        hrEmpForeignLanguageService.clearCertificateNameWriteIfNotApplicable(buffer);
        hrEmpForeignLanguageService.saveOrUpdateBatch(buffer);
        buffer.clear();
    }
}
