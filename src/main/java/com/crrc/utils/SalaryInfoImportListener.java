package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class SalaryInfoImportListener implements ReadListener<HrEmpSalaryInfo> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpSalaryInfo> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public SalaryInfoImportListener(HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService) {
        this.hrEmpSalaryInfoService = hrEmpSalaryInfoService;
    }

    @Override
    public void invoke(HrEmpSalaryInfo row, AnalysisContext analysisContext) {
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

        buffer.add(row);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }

        List<String> collect = buffer.stream().map(HrEmpSalaryInfo::getUserCode).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpSalaryInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HrEmpSalaryInfo::getUserCode, collect);
        List<HrEmpSalaryInfo> hrEmpSalaryInfos = hrEmpSalaryInfoService.getBaseMapper().selectList(wrapper);
        hrEmpSalaryInfos.forEach(item -> {
//            item.setPositionSalary("0");
            // 将String类型的seniorityStandard转为整数加一后再转回String
            int currentValue = Integer.parseInt(item.getSeniorityStandard());
            item.setSeniorityStandard(String.valueOf(currentValue + 1));
        });
        hrEmpSalaryInfoService.updateBatchById(hrEmpSalaryInfos);

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
