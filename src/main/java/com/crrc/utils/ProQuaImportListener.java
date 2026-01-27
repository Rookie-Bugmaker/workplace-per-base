package com.crrc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpProfessionalQualification;
import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.service.impl.HrEmpProfessionalQualificationServiceImpl;
import com.crrc.service.impl.HrEmpSkillLevelServiceImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class ProQuaImportListener implements ReadListener<HrEmpProfessionalQualification> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpProfessionalQualification> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService;
    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;

    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public ProQuaImportListener(HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService,
                                HrEmpBasicServiceImpl hrEmpBasicService,
                                HrEmpSkillLevelServiceImpl hrEmpSkillLevelService) {
        this.hrEmpProfessionalQualificationService = hrEmpProfessionalQualificationService;
        this.hrEmpBasicService = hrEmpBasicService;
        this.hrEmpSkillLevelService = hrEmpSkillLevelService;
    }

    @Override
    public void invoke(HrEmpProfessionalQualification row, AnalysisContext analysisContext) {
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
        List<HrEmpProfessionalQualification> proQuaList = new ArrayList<>(buffer);

        List<String> userCodeList = proQuaList.stream().map(HrEmpProfessionalQualification::getUserCode).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpProfessionalQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HrEmpProfessionalQualification::getUserCode, userCodeList);
        Map<String, List<HrEmpProfessionalQualification>> quaMap = hrEmpProfessionalQualificationService.getBaseMapper().selectList(wrapper).stream().collect(
                Collectors.groupingBy(
                        HrEmpProfessionalQualification::getUserCode
                )
        );

        List<HrEmpProfessionalQualification> changeList = new ArrayList<>();

        Map<String, HrEmpBasicDTO> basicMap = hrEmpBasicService.selectMsgList(userCodeList).stream().collect(Collectors.toMap(
                HrEmpBasic::getUserCode,
                e -> e,
                (a, b) -> a
        ));

        List<HrEmpBasic> basicList = new ArrayList<>();

        for (String userCode : userCodeList) {
            List<HrEmpProfessionalQualification> qualifications = quaMap.get(userCode);
            qualifications.forEach(item -> {
                if("0".equals(item.getIsHighest()) && "1".equals(item.getIsAppointed())) {
                    item.setIsAppointed("0");
                }
            });

            changeList.addAll(qualifications);

            HrEmpBasic basic = basicMap.get(userCode);
            basic.setCategory7(resolveCategory7(basic));

            basicList.add(basic);
        }

        hrEmpProfessionalQualificationService.updateBatchById(changeList);

        hrEmpBasicService.updateBatchById(basicList);

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

    public String resolveCategory7(HrEmpBasic hrEmpBasic) {
        try {
            String category7_pro = null;
            // 获取专业职务任职资格列表
            List<HrEmpProfessionalQualification> qualificationList = hrEmpProfessionalQualificationService.selectMsg(hrEmpBasic.getUserCode());
            if (!qualificationList.isEmpty()) {
                for (HrEmpProfessionalQualification qualification : qualificationList) {
                    // 找到目前最高的任职资格，以此为判断标准
                    if (StringUtils.isNotBlank(qualification.getIsHighest()) && SystemConstants.TRUE_FLAG.equals(qualification.getIsHighest()) && StringUtils.isNotBlank(qualification.getQuaTitleLevel())) {
                        if(qualification.getQuaTitleLevel().equals("00010") || qualification.getQuaTitleLevel().contains("正高")) {
                            category7_pro = "001";
                        } else if(qualification.getQuaTitleLevel().equals("00020") || qualification.getQuaTitleLevel().contains("副高")) {
                            category7_pro = "002";
                        } else if(qualification.getQuaTitleLevel().equals("00030") || qualification.getQuaTitleLevel().contains("中级")) {
                            category7_pro = "003";
                        } else if(qualification.getQuaTitleLevel().equals("00040") || qualification.getQuaTitleLevel().contains("助理级")) {
                            category7_pro = "004";
                        } else if(qualification.getQuaTitleLevel().equals("00050") || qualification.getQuaTitleLevel().contains("员级")) {
                            category7_pro = "005";
                        }
                    }
                }
            }
            String category7_skl = null;
            // 职业技能等级
            List<HrEmpSkillLevel> skillLevelList = hrEmpSkillLevelService.selectMsg(hrEmpBasic.getUserCode());
            if (!skillLevelList.isEmpty()) {
                for (HrEmpSkillLevel skillLevel : skillLevelList) {
                    // 找到目前最高的任职资格，以此为判断标准
                    if (StringUtils.isNotBlank(skillLevel.getIsHighest()) && SystemConstants.TRUE_FLAG.equals(skillLevel.getIsHighest())) {
                        if (StringUtils.isNotBlank(skillLevel.getSkillLevel())) {
                                                     /*
                                  00010/0020/0030	高级技师、特级技师、首席技师 高
                                  00040	技师 师
                                  00050	高级工 助
                                  00060/70	中级工、初级工 员
                                 */
                            if(skillLevel.getSkillLevel().equals("00010") || skillLevel.getSkillLevel().contains("首席技师")) {
                                category7_skl = "002";
                            } else if(skillLevel.getSkillLevel().equals("00020") || skillLevel.getSkillLevel().contains("特级技师")) {
                                category7_skl = "002";
                            } else if(skillLevel.getSkillLevel().equals("00030") || skillLevel.getSkillLevel().contains("高级技师")) {
                                category7_skl = "002";
                            } else if(skillLevel.getSkillLevel().equals("00040") || skillLevel.getSkillLevel().contains("技师")) {
                                category7_skl = "003";
                            } else if(skillLevel.getSkillLevel().equals("00050") || skillLevel.getSkillLevel().contains("高级工")) {
                                category7_skl = "004";
                            } else if(skillLevel.getSkillLevel().equals("00060") || skillLevel.getSkillLevel().contains("中级工")) {
                                category7_skl = "005";
                            } else if(skillLevel.getSkillLevel().equals("00070") || skillLevel.getSkillLevel().contains("初级工")) {
                                category7_skl = "005";
                            }
                        }
                    }
                }
            }
            // 如果是双通道评聘，取高的一条
            // 如果新加了码值，请处理这里的技师判断逻辑
            return CodeValueComparatorUtils.compareCategory7(category7_pro, category7_skl) < 0 ? category7_pro : category7_skl;
        } catch (Exception e) {
            return hrEmpBasic.getCategory7();
        }
    }
}
