package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson2.util.UUIDUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpPaper;
import com.crrc.service.impl.HrEmpPaperServiceImpl;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaperImportListener implements ReadListener<HrEmpPaper> {
    //论文分类
    private final Map<String, String> categoryTranslateMap = ImmutableMap.of(
            "专题型", "0",
            "论辩型", "1",
            "综述型", "2",
            "综合型", "3",
            "其他", "4"
    );

    //论文/论著
    private final Map<String, String> typeTranslateMap = ImmutableMap.of(
            "论文", "0",
            "论著", "1",
            "职业教材", "2"
    );

    //论文论著刊物级别
    private final Map<String, String> journalLevelTranslateMap = ImmutableMap.of(
            "国际级", "1",
            "国家级", "2",
            "省部级（全国性行业协会）", "3",
            "铁总级", "4",
            "集团级", "5",
            "地市级", "6",
            "子公司级", "7",
            "部门级", "8",
            "其他", "9"
    );

    //论文论著奖励级别
    private final Map<String, String> awardLevelTranslateMap = ImmutableMap.of(
            "国际级", "1",
            "国家级", "2",
            "省部级（全国性行业协会）", "3",
            "铁总级", "4",
            "集团级", "5",
            "地市级", "6",
            "子公司级", "7",
            "部门级", "8",
            "其他", "9"
    );

    //论文论著奖励等次
    private final Map<String, String> awardRankTranslateMap = ImmutableMap.of(
            "特等奖", "0",
            "一等奖", "1",
            "二等奖", "2",
            "三等奖", "3",
            "优秀奖", "4",
            "其他", "5"
    );

    private final HrEmpPaperServiceImpl paperService;

    private final List<HrEmpPaper> buffer = new ArrayList<>();

    public PaperImportListener(HrEmpPaperServiceImpl paperService) {
        this.paperService = paperService;
    }


    @Override
    public void invoke(HrEmpPaper hrEmpPaper, AnalysisContext analysisContext) {
        hrEmpPaper.setId(IdUtil.randomUUID());
        hrEmpPaper.setPaperCategory(categoryTranslateMap.getOrDefault(hrEmpPaper.getPaperCategory(), hrEmpPaper.getPaperCategory()));
        hrEmpPaper.setPaperType(typeTranslateMap.getOrDefault(hrEmpPaper.getPaperType(), hrEmpPaper.getPaperType()));
        hrEmpPaper.setJournalLevel(journalLevelTranslateMap.getOrDefault(hrEmpPaper.getJournalLevel(), hrEmpPaper.getJournalLevel()));
        hrEmpPaper.setAwardLevel(awardLevelTranslateMap.getOrDefault(hrEmpPaper.getAwardLevel(), hrEmpPaper.getAwardLevel()));
        hrEmpPaper.setAwardRank(awardRankTranslateMap.getOrDefault(hrEmpPaper.getAwardRank(), hrEmpPaper.getAwardRank()));
        buffer.add(hrEmpPaper);
        if(buffer.size() > 1000) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    private void flush() {
        paperService.saveOrUpdateBatch(buffer);
    }
}
