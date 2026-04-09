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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaperImportListener implements ReadListener<HrEmpPaper> {
//    //论文分类
//    private final Map<String, String> categoryTranslateMap = ImmutableMap.of(
//            "专题型", "0",
//            "论辩型", "1",
//            "综述型", "2",
//            "综合型", "3",
//            "其他", "4"
//    );

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

    //是否被SCI、EI、ISTP检索
    private final Map<String, String> sciEiIstpTranslateMap = ImmutableMap.of(
            "SCI", "0",
            "EI", "1",
            "ISTP", "2",
            "否", "3"
    );

    //是否核心期刊
    private final Map<String, String> coreJournalTranslateMap = ImmutableMap.of(
            "否", "0",
            "是", "1"
    );

    private final HrEmpPaperServiceImpl paperService;

    private final List<HrEmpPaper> buffer = new ArrayList<>();

    public PaperImportListener(HrEmpPaperServiceImpl paperService) {
        this.paperService = paperService;
    }

    @Override
    public void invoke(HrEmpPaper hrEmpPaper, AnalysisContext analysisContext) {
        // 必填
        if(hrEmpPaper.getPublishDate() == null) {
            throw new IllegalArgumentException("发表日期必填");
        }
        if(!StringUtils.hasText(hrEmpPaper.getPaperName())) {
            throw new IllegalArgumentException("论文（专著/教材）名称必填");
        }
        if(!StringUtils.hasText(hrEmpPaper.getJournalName())) {
            throw new IllegalArgumentException("刊物/出版社名称必填");
        }
        try {
            if(!StringUtils.hasText(hrEmpPaper.getPersonRank()) && Integer.parseInt(hrEmpPaper.getPersonRank()) > 20) {
                throw new IllegalArgumentException("个人排名必填，且必须20以下");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("个人排名必须为纯数字");
        }
        if(!StringUtils.hasText(hrEmpPaper.getTotalRank())) {
            throw new IllegalArgumentException("排名总人数必填");
        }
        if(!sciEiIstpTranslateMap.containsKey(hrEmpPaper.getIsSciEiIstp())) {
            throw new IllegalStateException("是否被SCI、EI、ISTP检索填写有误，应为SCI、EI、ISTP或否");
        }
        if(!typeTranslateMap.containsKey(hrEmpPaper.getPaperType())) {
            throw new IllegalStateException("论文/论著填写有误，应为论文、论著或职业教材");
        }
        if(!coreJournalTranslateMap.containsKey(hrEmpPaper.getIsCoreJournal())) {
            throw new IllegalStateException("是否和核心期刊填写有误，应为是或否");
        }
        if(!journalLevelTranslateMap.containsKey(hrEmpPaper.getJournalLevel())) {
            throw new IllegalStateException("论文论著刊物级别填写有误，应为国际级、国家级、省部级（全国性行业协会）等");
        }
        // 选填
        if(StringUtils.hasText(hrEmpPaper.getAwardLevel()) && !awardLevelTranslateMap.containsKey(hrEmpPaper.getAwardLevel())) {
            throw new IllegalStateException("论文论著奖励级别填写有误，应为国际级、国家级、省部级（全国性行业协会）等");
        }
        if(StringUtils.hasText(hrEmpPaper.getAwardRank()) && !awardRankTranslateMap.containsKey(hrEmpPaper.getAwardRank())) {
            throw new IllegalStateException("论文论著奖励等次填写有误，应为特等奖、一等奖、二等奖、三等奖、优秀奖或其他");
        }

        hrEmpPaper.setId(IdUtil.randomUUID());
        hrEmpPaper.setIsSciEiIstp(sciEiIstpTranslateMap.getOrDefault(hrEmpPaper.getIsSciEiIstp(), hrEmpPaper.getIsSciEiIstp()));
        //hrEmpPaper.setPaperCategory(categoryTranslateMap.getOrDefault(hrEmpPaper.getPaperCategory(), hrEmpPaper.getPaperCategory()));
        hrEmpPaper.setPaperType(typeTranslateMap.getOrDefault(hrEmpPaper.getPaperType(), hrEmpPaper.getPaperType()));
        hrEmpPaper.setJournalLevel(journalLevelTranslateMap.getOrDefault(hrEmpPaper.getJournalLevel(), hrEmpPaper.getJournalLevel()));
        hrEmpPaper.setAwardLevel(awardLevelTranslateMap.getOrDefault(hrEmpPaper.getAwardLevel(), hrEmpPaper.getAwardLevel()));
        hrEmpPaper.setAwardRank(awardRankTranslateMap.getOrDefault(hrEmpPaper.getAwardRank(), hrEmpPaper.getAwardRank()));
        hrEmpPaper.setIsCoreJournal(coreJournalTranslateMap.getOrDefault(hrEmpPaper.getIsCoreJournal(), hrEmpPaper.getIsCoreJournal()));
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
        buffer.clear();
    }
}
