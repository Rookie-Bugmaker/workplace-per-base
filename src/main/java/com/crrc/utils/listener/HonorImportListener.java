package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpHonor;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.service.impl.HrEmpHonorServiceImpl;
import com.crrc.utils.DeptUtils;
import com.google.common.collect.ImmutableMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HonorImportListener implements ReadListener<HrEmpHonor> {

    //荣誉称号类别
    private final Map<String, String> titleTypeTranslateMap = ImmutableMap.of(
            "综合", "0",
            "单项", "1"
    );
    // 集体/个人
    private final Map<String, String> groupOrPersonalTranslateMap = ImmutableMap.of(
            "集体", "1",
            "个人", "2"
    );
    //荣誉奖励类别
    private final Map<String, String> rewardTypeTranslateMap = ImmutableMap.of(
            "荣誉称号", "1",
            "奖励", "2",
            "其他", "3"
    );
    // 归口单位
    private final Map<String, String> responsibleUnitTranslateMap = ImmutableMap.of(
            "党委组织部", "1",
            "党委宣传部", "2",
            "团委", "3",
            "工会", "4",
            "人力资源部", "5",
            "其他", "6"
    );

    // 荣誉取得方式
    private final Map<String, String> obtainMethodTranslateMap = ImmutableMap.of(
            "评选", "0",
            "竞赛", "1",
            "选举", "2"
    );

    // 荣誉所属类别
    private final Map<String, String> categoryTranslateMap = ImmutableMap.of(
            "人才荣誉类", "0",
            "人才奖励类", "1",
            "人才培养类", "2",
            "人才认定类", "3",
            "集体荣誉类", "4",
            "集体奖励类", "5",
            "集体认定类", "6"
    );

    private final Map<String, String> honorCategoriesTranslateMap = ImmutableMap.<String, String>builder()
            .put("中国科学院院士R01", "0")
            .put("中国科学院院士R0101", "1")
            .put("中国工程院院士R02", "2")
            .put("中国工程院院士R0201", "3")
            .put("国家卓越工程师奖R03", "4")
            .put("国家卓越工程师奖R0301", "5")
            .put("国家卓越工程师团队R0302", "6")
            .put("工会类荣誉R05", "7")
            .put("劳动模范R0501", "8")
            .put("劳动奖章（奖状）R0502", "9")
            .put("大国工匠R0503", "10")
            .put("火车头奖章R0504", "11")
            .put("工人先锋号R0505", "12")
            .put("政府特殊津贴R06", "13")
            .put("享受国务院政府特殊津贴R0601", "14")
            .put("高层次人才R07", "15")
            .put("交通运输行业中青年科技领军人才30", "16")
            .put("铁路青年人才托举工程（青年托举工程）33", "17")
            .put("百-万-百万人才工程34", "18")
            .put("高层次人才特殊支持计划科技领军人才（万人计划）36", "19")
            .put("科技创业领军人才（经营管理-万人计划）37", "20")
            .put("高层次人才特殊支持计划青年拔尖人才（小万人计划）39", "21")
            .put("青年科技创新领军人才（创新人才推进计划）40", "22")
            .put("百千万人才工程国家级人选41", "23")
            .put("有突出贡献中青年专家42", "24")
            .put("高端外国专家引进计划43", "25")
            .put("海外高层次人才博士后专项44", "26")
            .put("长城计划R08", "27")
            .put("长城计划（CC）38", "28")
            .put("长城计划青年人才R0801", "29")
            .put("长城计划创新人才R0802", "30")
            .put("火炬计划R09", "31")
            .put("火炬计划青年人才R0901", "32")
            .put("火炬计划创新人才R0902", "33")
            .put("启明计划R10", "34")
            .put("启明计划青年人才R1001", "35")
            .put("启明计划创新人才R1011", "36")
            .put("茅以升奖R11", "37")
            .put("茅以升科学技术奖R1101", "38")
            .put("茅以升铁道工程师奖R1102", "39")
            .put("詹天佑奖R12", "40")
            .put("詹科大奖20", "41")
            .put("詹科成就奖21", "42")
            .put("詹科人才奖22", "43")
            .put("詹科青年奖23", "44")
            .put("詹科贡献奖24", "45")
            .put("詹专青年奖25", "46")
            .put("詹专贡献奖26", "47")
            .put("光华工程科技奖R13", "48")
            .put("光华工程科技奖R1301", "49")
            .put("何梁何利科技奖R14", "50")
            .put("何梁何利科技奖R1401", "51")
            .put("技术技能类荣誉R15", "52")
            .put("省技术能手19", "53")
            .put("中华技能大奖5", "54")
            .put("全国技术能手7", "55")
            .put("中央企业技术能手8", "56")
            .put("中国中车技术能手9", "57")
            .put("中国中车技术标兵R1504", "58")
            .put("全国交通技术能手R1506", "59")
            .put("中央企业优秀科技领军人才R1508", "60")
            .put("中央企业优秀青年科技人才R1509", "61")
            .put("中央企业优秀技术团队技术能手R1510", "62")
            .put("杰出青年荣誉R16", "63")
            .put("十大杰出青年R1601", "64")
            .put("五四青年奖章R1602", "65")
            .put("青年技术能手R1603", "66")
            .put("青年岗位技术能手R1604", "67")
            .put("杰出女性荣誉R17", "68")
            .put("三八红旗手3", "69")
            .put("青年女科学家31", "70")
            .put("未来女科学家32", "71")
            .put("财务管理荣誉R18", "72")
            .put("高端会计人才培养工程10", "73")
            .put("财政部领军人才11", "74")
            .put("财政部国际化人才12", "75")
            .put("省级会计领军人才13", "76")
            .put("集团财务先进个人14", "77")
            .put("全国会计先进工作者15", "78")
            .put("集团财务管理专家16", "79")
            .put("其他R99", "80")
            .put("中国政府友谊奖R9901", "81")
            .put("中央企业大国工匠R9902", "82")
            .put("国家改革先锋R9903", "83")
            .put("其他R9999", "84")
            .build();

    HrEmpHonorServiceImpl hrEmpHonorService;

    List<HrEmpHonor> buffer = new ArrayList<>();

    public HonorImportListener(HrEmpHonorServiceImpl hrEmpHonorService) {
        this.hrEmpHonorService = hrEmpHonorService;
    }

    @Override
    public void invoke(HrEmpHonor hrEmpHonor, AnalysisContext analysisContext) {
        // 必填
        hrEmpHonor.setId(IdUtil.randomUUID());
        if(hrEmpHonor.getAwardDate() == null) {
            throw new IllegalArgumentException("获奖日期必填");
        }
        if(!StringUtils.hasText(hrEmpHonor.getTitleLevel())) {
            throw new IllegalArgumentException("称号级别必填");
        }
        if(!StringUtils.hasText(hrEmpHonor.getAwardYear())) {
            throw new IllegalArgumentException("获奖年度必填");
        }
        if(!StringUtils.hasText(hrEmpHonor.getHonorName())) {
            throw new IllegalArgumentException("荣誉名称必填");
        }
        if(!titleTypeTranslateMap.containsKey(hrEmpHonor.getTitleType())) {
            throw new IllegalArgumentException("荣誉称号类别填写有误，应为综合或单项");
        }
        if(!groupOrPersonalTranslateMap.containsKey(hrEmpHonor.getGroupOrPersonal())) {
            throw new IllegalArgumentException("集体/个人填写有误，应为集体或个人");
        }
        if(!honorCategoriesTranslateMap.containsKey(hrEmpHonor.getHonorType())) {
            throw new IllegalArgumentException("荣誉类别填写有误，应为中国科学院院士R01、青年岗位技术能手R1604、其他R9999等");
        }
        if(!responsibleUnitTranslateMap.containsKey(hrEmpHonor.getResponsibleUnit())) {
            throw new IllegalArgumentException("归口单位填写有误，应为党委组织部、党委宣传部、团委、工会、人力资源部或其他");
        }
        // 选填
        if(StringUtils.hasText(hrEmpHonor.getRewardType()) && !rewardTypeTranslateMap.containsKey(hrEmpHonor.getRewardType())) {
            throw new IllegalArgumentException("荣誉奖励类别填写有误，应为荣誉称号、奖励或其他");
        }
        if(StringUtils.hasText(hrEmpHonor.getObtainMethod()) && !obtainMethodTranslateMap.containsKey(hrEmpHonor.getObtainMethod())) {
            throw new IllegalArgumentException("荣誉取得方式填写有误，应为评选、竞赛或选举");
        }
        if(StringUtils.hasText(hrEmpHonor.getCategory()) && !categoryTranslateMap.containsKey(hrEmpHonor.getCategory())) {
            throw new IllegalArgumentException("荣誉所属类别填写有误，应为人才荣誉类、人才奖励类、集体奖励类等");
        }

        hrEmpHonor.setTitleType(titleTypeTranslateMap.getOrDefault(hrEmpHonor.getTitleType(), hrEmpHonor.getTitleType()));
        hrEmpHonor.setGroupOrPersonal(groupOrPersonalTranslateMap.getOrDefault(hrEmpHonor.getGroupOrPersonal(), hrEmpHonor.getGroupOrPersonal()));
        hrEmpHonor.setRewardType(rewardTypeTranslateMap.getOrDefault(hrEmpHonor.getRewardType(), hrEmpHonor.getRewardType()));
        hrEmpHonor.setResponsibleUnit(responsibleUnitTranslateMap.getOrDefault(hrEmpHonor.getResponsibleUnit(), hrEmpHonor.getResponsibleUnit()));
        hrEmpHonor.setObtainMethod(obtainMethodTranslateMap.getOrDefault(hrEmpHonor.getObtainMethod(), hrEmpHonor.getObtainMethod()));
        hrEmpHonor.setCategory(categoryTranslateMap.getOrDefault(hrEmpHonor.getCategory(), hrEmpHonor.getCategory()));
        hrEmpHonor.setHonorType(honorCategoriesTranslateMap.getOrDefault(hrEmpHonor.getHonorType(), hrEmpHonor.getHonorType()));

        buffer.add(hrEmpHonor);
        if(buffer.size() > 1000) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    private void flush() {
        hrEmpHonorService.saveOrUpdateBatch(buffer);
        buffer.clear();
    }
}
