package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.entity.HrEmpSalaryStandard;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;
import com.crrc.service.impl.HrEmpSalaryStandardServiceImpl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class SalaryStandardChangeImportListener implements ReadListener<HrEmpSalaryStandard> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpSalaryStandard> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpSalaryStandardServiceImpl hrEmpSalaryStandardService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public SalaryStandardChangeImportListener(HrEmpSalaryStandardServiceImpl hrEmpSalaryStandardService) {
        this.hrEmpSalaryStandardService = hrEmpSalaryStandardService;
    }

    @Override
    public void invoke(HrEmpSalaryStandard row, AnalysisContext analysisContext) {
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

        List<HrEmpSalaryStandard> list = new ArrayList<>();

        buffer.forEach(item -> {
            LambdaQueryWrapper<HrEmpSalaryStandard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpSalaryStandard::getUserCode, item.getUserCode());
            wrapper.last("ORDER BY effective_date DESC NULLS LAST, create_time DESC NULLS LAST");

            List<HrEmpSalaryStandard> hrEmpSalaryStandards = hrEmpSalaryStandardService.getBaseMapper().selectList(wrapper);
            HrEmpSalaryStandard hrEmpSalaryStandard = hrEmpSalaryStandards.get(0);
            hrEmpSalaryStandard.setSalaryGrade("0");
            hrEmpSalaryStandard.setCoefficient("0");

            list.add(hrEmpSalaryStandard);
        });

        hrEmpSalaryStandardService.updateBatchById(list);

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
