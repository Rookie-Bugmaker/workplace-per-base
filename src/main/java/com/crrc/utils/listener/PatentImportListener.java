package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpPatent;
import com.crrc.service.impl.HrEmpPatentServiceImpl;
import com.google.common.collect.ImmutableMap;
import org.springframework.util.StringUtils;

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

    //是否获奖
    private final Map<String, String> isAwardedTranslateMap = ImmutableMap.of(
            "否", "0",
            "是", "1"
    );

    // 获奖级别
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

    private final HrEmpPatentServiceImpl patentService;

    private final List<HrEmpPatent> buffer = new ArrayList<>();

    public PatentImportListener(HrEmpPatentServiceImpl patentService) {
        this.patentService = patentService;
    }

    @Override
    public void invoke(HrEmpPatent hrEmpPatent, AnalysisContext analysisContext) {
        hrEmpPatent.setId(IdUtil.randomUUID());
        // 必填
        if(hrEmpPatent.getAuthorizationDate() == null) {
            throw new IllegalArgumentException("授权日期必填");
        }
        if(!StringUtils.hasText(hrEmpPatent.getPatentName())) {
            throw new IllegalArgumentException("专利/软著名称必填");
        }
        if(!StringUtils.hasText(hrEmpPatent.getPatentNo())) {
            throw new IllegalArgumentException("专利号必填");
        }
        // 必填且 要么为其他，要么在20及以下
        try {
            if(
                    !StringUtils.hasText(hrEmpPatent.getPersonalRank()) ||
                            (!"其他".equals(hrEmpPatent.getPersonalRank()) && Integer.parseInt(hrEmpPatent.getPersonalRank()) > 20)
            ) {
                throw new IllegalArgumentException("个人排名必填，且必须20以下，或为其他");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("个人排名必须为纯数字或其他");
        }
        // 必填且 要么为其他，要么在20及以下
        try {
            if(
                    !StringUtils.hasText(hrEmpPatent.getTotalParticipants()) ||
                            (!"其他".equals(hrEmpPatent.getTotalParticipants()) && Integer.parseInt(hrEmpPatent.getTotalParticipants()) > 20)
            ) {
                throw new IllegalArgumentException("排名总人数必填，且必须20以下，或为其他");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("排名总人数必须为纯数字或其他");
        }

        if(!StringUtils.hasText(hrEmpPatent.getAppraisalAuthority())) {
            throw new IllegalArgumentException("鉴定单位必填");
        }
        if(!StringUtils.hasText(hrEmpPatent.getCreationDate())) {
            throw new IllegalArgumentException("创作时间必填");
        }
        if(!patentTypeTranslateMap.containsKey(hrEmpPatent.getPatentType())) {
            throw new IllegalArgumentException("专利类型填写有误，应为发明、实用新型、外观设计或软件著作权");
        }
        if(!patentLevelTranslateMap.containsKey(hrEmpPatent.getPatentLevel())) {
            throw new IllegalArgumentException("专利级别填写有误，应为国内或国外");
        }
        // 选填
        if(StringUtils.hasText(hrEmpPatent.getAwardGrade()) && !awardGradeTranslateMap.containsKey(hrEmpPatent.getAwardGrade())) {
            throw new IllegalArgumentException("专利奖励等次填写有误，应为金奖、银奖、一等奖、二等奖、三等奖或优秀奖");
        }
        if(StringUtils.hasText(hrEmpPatent.getStatus()) && !patentStatusTranslateMap.containsKey(hrEmpPatent.getStatus())) {
            throw new IllegalArgumentException("专利状态填写有误，应为申请或已授权");
        }
        if(StringUtils.hasText(hrEmpPatent.getIsAwarded()) && !isAwardedTranslateMap.containsKey(hrEmpPatent.getIsAwarded())) {
            throw new IllegalArgumentException("是否获奖填写有误，应为是或否");
        }
        if(StringUtils.hasText(hrEmpPatent.getAwardLevel()) && !awardLevelTranslateMap.containsKey(hrEmpPatent.getAwardLevel())) {
            throw new IllegalArgumentException("获奖级别填写有误，应为国际级、国家级、省部级（全国性行业协会）等");
        }

        hrEmpPatent.setPatentType(patentTypeTranslateMap.getOrDefault(hrEmpPatent.getPatentType(), hrEmpPatent.getPatentType()));
        hrEmpPatent.setPatentLevel(patentLevelTranslateMap.getOrDefault(hrEmpPatent.getPatentLevel(), hrEmpPatent.getPatentLevel()));
        hrEmpPatent.setAwardGrade(awardGradeTranslateMap.getOrDefault(hrEmpPatent.getAwardGrade(), hrEmpPatent.getAwardGrade()));
        hrEmpPatent.setStatus(patentStatusTranslateMap.getOrDefault(hrEmpPatent.getStatus(), hrEmpPatent.getStatus()));
        hrEmpPatent.setIsAwarded(isAwardedTranslateMap.getOrDefault(hrEmpPatent.getIsAwarded(), hrEmpPatent.getIsAwarded()));
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
