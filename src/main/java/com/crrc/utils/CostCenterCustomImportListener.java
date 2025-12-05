package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpBasic;
import com.crrc.pojo.vo.CostCenterImportVO;
import com.crrc.service.impl.HrEmpBasicServiceImpl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

public class CostCenterCustomImportListener implements ReadListener<CostCenterImportVO> {

    private static final int BATCH_SIZE = 1000;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字，长度你可按需调整

    private final HrEmpBasicServiceImpl hrEmpBasicService;

    private final LinkedHashSet<String> buffer = new LinkedHashSet<>();
    private final List<String> errors = new ArrayList<>();
    private final Long costCenterId;
    private int rowIndex = 0;

    public CostCenterCustomImportListener(HrEmpBasicServiceImpl hrEmpBasicService, Long costCenterId) {
        this.hrEmpBasicService = hrEmpBasicService;
        this.costCenterId = costCenterId;
    }

    @Override
    public void invoke(CostCenterImportVO data, AnalysisContext context) {
        rowIndex++;
        String raw = (data == null) ? null : data.getArchiveNo();
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

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flush();
        if (!errors.isEmpty()) {
            // 这里可以抛出自定义异常，或返回给前端
            throw new IllegalArgumentException("导入完成，但存在错误：\n" + String.join("\n", errors));
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        ArrayList<String> userCodeList = new ArrayList<>(buffer);

        for (String userCode : userCodeList) {
            HrEmpBasic hrEmpBasic = hrEmpBasicService.getByUserCode(userCode);
            hrEmpBasic.setCostCenter(costCenterId.toString());
            hrEmpBasicService.updateById(hrEmpBasic);
        }

        buffer.clear();
    }
}