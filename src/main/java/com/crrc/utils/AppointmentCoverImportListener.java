package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.entity.PositionInfo;
import com.crrc.service.impl.HrEmpAppointmentServiceImpl;
import com.crrc.service.impl.PositionInfoServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author myy
 * @since 2026-1-12
 */
public class AppointmentCoverImportListener implements ReadListener<HrEmpAppointment> {

    private static final int BATCH_SIZE = 1000;
    private final List<HrEmpAppointment> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final HrEmpAppointmentServiceImpl hrEmpAppointmentService;
    private final Pattern DIGITS = Pattern.compile("^\\d{1,50}$"); // 只允许数字

    public AppointmentCoverImportListener(HrEmpAppointmentServiceImpl hrEmpAppointmentService) {
        this.hrEmpAppointmentService = hrEmpAppointmentService;
    }

    @Override
    public void invoke(HrEmpAppointment row, AnalysisContext analysisContext) {
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

        buffer.add(row);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }

        List<HrEmpAppointment> changeList = new ArrayList<>();

        for (HrEmpAppointment item : buffer) {
            String userCode = item.getUserCode();

            LambdaQueryWrapper<HrEmpAppointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpAppointment::getUserCode, userCode);
            wrapper.last("ORDER BY create_time DESC NULLS LAST, start_date DESC NULLS LAST, end_date DESC NULLS LAST");
            List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentService.getBaseMapper().selectList(wrapper);

            if(hrEmpAppointments.isEmpty() || hrEmpAppointments.size() < 2 || !StringUtils.hasText(hrEmpAppointments.get(1).getPosition())) {
                errors.add("任职信息查询失败，无前职位: " + userCode);
                continue;
            }

            HrEmpAppointment oldApp = hrEmpAppointments.get(1);
            HrEmpAppointment newApp = hrEmpAppointments.get(0);

            newApp.setPosition(oldApp.getPosition());
            newApp.setJobType(oldApp.getJobType());

            changeList.add(newApp);
        }

        hrEmpAppointmentService.updateBatchById(changeList);

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
