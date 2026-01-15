package com.crrc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpBasic;
import com.crrc.service.impl.HrEmpBasicServiceImpl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class CategoryImportListener implements ReadListener<HrEmpBasic> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpBasic> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public CategoryImportListener(HrEmpBasicServiceImpl hrEmpBasicService) {
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

        List<String> userCodeList = basicArrayList.stream().map(HrEmpBasic::getUserCode).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HrEmpBasic::getUserCode, userCodeList);
        Map<String, HrEmpBasic> hrEmpBasicMap = hrEmpBasicService.getBaseMapper().selectList(wrapper).stream().collect(
                Collectors.toMap(
                        HrEmpBasic::getUserCode,
                        e -> e,
                        (a, b) -> a
                )
        );

        for (HrEmpBasic datum : basicArrayList) {
            HrEmpBasic hrEmpBasic = hrEmpBasicMap.get(datum.getUserCode());
            BeanUtil.copyProperties(datum, hrEmpBasic, CopyOptions.create().setIgnoreNullValue(true));
        }

        hrEmpBasicService.updateBatchById(hrEmpBasicMap.values());

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
