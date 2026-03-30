package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpPatent;
import com.crrc.service.impl.HrEmpPatentServiceImpl;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatentImportListener implements ReadListener<HrEmpPatent> {

    //专利类型
    private final Map<String, String> patentTypeTranslateMap = ImmutableMap.of(
            "发明", "0",
            "实用新型", "1",
            "外观设计", "2",
            "软件著作权", "3"
    );

    //专利级别
    private final Map<String, String> patentLevelTranslateMap = ImmutableMap.of(
            "国内", "0",
            "国外", "1"
    );

    //专利奖励等次
    private final Map<String, String> awardGradeTranslateMap = ImmutableMap.of(
            "金奖", "0",
            "银奖", "1",
            "一等奖", "2",
            "二等奖", "3",
            "三等奖", "4",
            "优秀奖", "5"
    );

    //专利状态
    private final Map<String, String> patentStatusTranslateMap = ImmutableMap.of(
            "申请", "0",
            "已授权", "1"
    );

    private final HrEmpPatentServiceImpl patentService;

    private final List<HrEmpPatent> buffer = new ArrayList<>();

    public PatentImportListener(HrEmpPatentServiceImpl patentService) {
        this.patentService = patentService;
    }

    @Override
    public void invoke(HrEmpPatent hrEmpPatent, AnalysisContext analysisContext) {
        hrEmpPatent.setId(IdUtil.randomUUID());
        buffer.add(hrEmpPatent);
        if(buffer.size() > 1000) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    private void flush() {
        patentService.saveOrUpdateBatch(buffer);
        buffer.clear();
    }
}
