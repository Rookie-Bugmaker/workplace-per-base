package com.crrc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.service.impl.HrEmpSkillLevelServiceImpl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class SkillLevelChangeListener implements ReadListener<HrEmpSkillLevel> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpSkillLevel> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public SkillLevelChangeListener(HrEmpSkillLevelServiceImpl hrEmpSkillLevelService) {
        this.hrEmpSkillLevelService = hrEmpSkillLevelService;
    }

    @Override
    public void invoke(HrEmpSkillLevel row, AnalysisContext analysisContext) {
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
        ArrayList<HrEmpSkillLevel> skillLevels = new ArrayList<>(buffer);

        for (HrEmpSkillLevel level : skillLevels) {// 找到该用户前一条记录
            HrEmpSkillLevel skillLevel = new HrEmpSkillLevel();
            skillLevel.setJobId(level.getJobId());
            skillLevel.setUserCode(level.getUserCode());
            String levelTemp = level.getSkillLevel();
            int i = Integer.parseInt(levelTemp);
            i = i + 10;
            String format = String.format("%05d", i);

            skillLevel.setSkillLevel(format);
            List<HrEmpSkillLevel> list = hrEmpSkillLevelService.query(skillLevel);
            if (list.isEmpty()) {
                errors.add(String.format("未找到匹配的数据: %s, %s, %s", skillLevel.getJobId(), skillLevel.getUserCode(), skillLevel.getSkillLevel()));
                continue;
            }
            if (list.size() > 1) {
                errors.add(String.format("查询出了重复的数据: %s, %s, %s", skillLevel.getJobId(), skillLevel.getUserCode(), skillLevel.getSkillLevel()));
                continue;
            }

            list.get(0).setIsAppointed("1");
            list.get(0).setIsHighest("1");

            // 查出这个人所有的职业技能等级
            HrEmpSkillLevel hrEmpSkillLevel = new HrEmpSkillLevel();
            hrEmpSkillLevel.setUserCode(level.getUserCode());
            List<HrEmpSkillLevel> query = hrEmpSkillLevelService.query(hrEmpSkillLevel);
            query.forEach(item -> {
                item.setIsAppointed("0");
                item.setIsHighest("0");
            });

            hrEmpSkillLevelService.updateBatchById(query);

            hrEmpSkillLevelService.updateById(list.get(0));
        }

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
