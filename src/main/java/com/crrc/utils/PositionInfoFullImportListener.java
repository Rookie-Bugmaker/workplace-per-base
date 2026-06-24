package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.pojo.vo.PositionInfoFullImportVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位信息完整导入监听器。
 * 只负责读取Excel行，校验和落库交给Service统一处理，便于返回完整错误信息。
 */
public class PositionInfoFullImportListener implements ReadListener<PositionInfoFullImportVO> {
    private final List<PositionInfoFullImportVO> rows = new ArrayList<>();

    @Override
    public void invoke(PositionInfoFullImportVO data, AnalysisContext context) {
        rows.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // no-op
    }

    public List<PositionInfoFullImportVO> getRows() {
        return rows;
    }
}
