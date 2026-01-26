package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.PositionInfo;
import com.crrc.service.impl.PositionInfoServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author myy
 * @since 2026-1-12
 */
public class PositionInfoImportListener implements ReadListener<PositionInfo> {

    private static final int BATCH_SIZE = 1000;
    private final List<PositionInfo> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final PositionInfoServiceImpl positionInfoService;

    public PositionInfoImportListener(PositionInfoServiceImpl positionInfoService) {
        this.positionInfoService = positionInfoService;
    }

    @Override
    public void invoke(PositionInfo row, AnalysisContext analysisContext) {
        rowIndex++;

        if (row == null || !StringUtils.hasText(row.getCode())) {
            errors.add("第 " + rowIndex + " 行：职位编码为空");
            return;
        }

        if(row.getCode().startsWith("-")) {
            row.setCode(row.getCode().replaceFirst("-", ""));
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

        positionInfoService.saveBatch(buffer);

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
