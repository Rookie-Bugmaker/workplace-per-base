package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpBasic;
import com.crrc.service.impl.HrEmpBasicServiceImpl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author myy
 * @since 2026-1-12
 */
public class Emp2ZeroImportListener implements ReadListener<HrEmpBasic> {

    private static final int BATCH_SIZE = 1000;
    private final LinkedHashSet<String> buffer = new LinkedHashSet<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public Emp2ZeroImportListener(HrEmpBasicServiceImpl hrEmpBasicService) {
        this.hrEmpBasicService = hrEmpBasicService;
    }

    @Override
    public void invoke(HrEmpBasic hrEmpBasic, AnalysisContext analysisContext) {
        rowIndex++;
        String raw = (hrEmpBasic == null) ? null : hrEmpBasic.getUserCode();
        String val = raw == null ? null : raw.trim();

        if (val == null || val.isEmpty()) {
            errors.add("第 " + rowIndex + " 行：档案号为空");
            return;
        }
        if (!DIGITS.matcher(val).matches()) {
            errors.add("第 " + rowIndex + " 行：档案号必须为纯数字（实际为：" + val + "）");
            return;
        }
        // 去重：LinkedHashSet 保持原有顺序
        buffer.add(val);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        ArrayList<String> userCodeList = new ArrayList<>(buffer);

        List<HrEmpBasic> data = new ArrayList<>();

        for (String userCode : userCodeList) {
            HrEmpBasic hrEmpBasic = hrEmpBasicService.getByUserCode(userCode);
            hrEmpBasic.setIsEmployed("0");
            data.add(hrEmpBasic);
        }

        hrEmpBasicService.removeBatchByIds(data);

        buffer.clear();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
        if (!errors.isEmpty()) {
            // 这里可以抛出自定义异常，或返回给前端
            throw new IllegalArgumentException("导入完成，但存在错误：\n" + String.join("\n", errors));
        }
    }
}
