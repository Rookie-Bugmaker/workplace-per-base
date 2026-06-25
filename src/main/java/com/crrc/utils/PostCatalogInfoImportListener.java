package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.pojo.vo.PostCatalogInfoImportVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形职务族导入监听器。
 * 仅负责读取Excel行，业务校验和落库交给Service统一处理。
 */
public class PostCatalogInfoImportListener implements ReadListener<PostCatalogInfoImportVO> {
    private final List<PostCatalogInfoImportVO> rows = new ArrayList<>();

    @Override
    public void invoke(PostCatalogInfoImportVO data, AnalysisContext context) {
        rows.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // no-op
    }

    public List<PostCatalogInfoImportVO> getRows() {
        return rows;
    }
}
