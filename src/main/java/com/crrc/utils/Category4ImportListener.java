package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpBasic;
import com.crrc.service.impl.HrEmpBasicServiceImpl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author myy
 * @since 2026-1-12
 */
public class Category4ImportListener implements ReadListener<HrEmpBasic> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpBasic> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public Category4ImportListener(HrEmpBasicServiceImpl hrEmpBasicService) {
        this.hrEmpBasicService = hrEmpBasicService;
    }

    @Override
    public void invoke(HrEmpBasic row, AnalysisContext analysisContext) {
        rowIndex++;
        String raw = (row == null) ? null : row.getUserCode();
        String val = raw == null ? null : raw.trim();

        if (val == null || val.isEmpty()) {
            errors.add("第 " + rowIndex + " 行：档案号为空");
            return;
        }
        if (!DIGITS.matcher(val).matches()) {
            errors.add("第 " + rowIndex + " 行：档案号必须为纯数字（实际为：" + val + "）");
            return;
        }

        buffer.add(row);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        ArrayList<HrEmpBasic> basicArrayList = new ArrayList<>(buffer);

        List<HrEmpBasic> data = new ArrayList<>();

        for (HrEmpBasic datum : basicArrayList) {
            HrEmpBasic hrEmpBasic = hrEmpBasicService.getByUserCode(datum.getUserCode());
            hrEmpBasic.setCategory4(datum.getCategory4());
            data.add(hrEmpBasic);
        }

        hrEmpBasicService.updateBatchById(data);

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
