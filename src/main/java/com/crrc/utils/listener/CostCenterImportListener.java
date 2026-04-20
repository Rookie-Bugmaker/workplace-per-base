package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CostCenterImportListener implements ReadListener<HrEmpSalaryInfo> {

    private final List<String> errors = new ArrayList<>();

    HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;

    List<HrEmpSalaryInfo> buffer = new ArrayList<>();

    private int rowIndex = 0;

    public CostCenterImportListener(HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService) {
        this.hrEmpSalaryInfoService = hrEmpSalaryInfoService;
    }

    @Override
    public void invoke(HrEmpSalaryInfo hrEmpSalaryInfo, AnalysisContext analysisContext) {

        try{
            rowIndex++;
            // 必填项校验
            if (!StringUtils.hasText(hrEmpSalaryInfo.getUserCode())) {
                errors.add("第 " + rowIndex + "行，存在空档案号，请检查excel");
                return;
            }

            List<HrEmpSalaryInfo> existMsgList = hrEmpSalaryInfoService.selectMsg(hrEmpSalaryInfo.getUserCode());
            if(existMsgList != null && !existMsgList.isEmpty()) {
                existMsgList.get(0).setDepartmentCode(hrEmpSalaryInfo.getDepartmentCode());
                buffer.add(existMsgList.get(0));
            } else {
                errors.add("第 " + rowIndex + "行，该人员无薪资信息，不计入本部工资系统，无法导入成本中心，请删除人员后重试");
                return;
//                hrEmpSalaryInfo.setId(IdUtil.simpleUUID());
//                buffer.add(hrEmpSalaryInfo);
            }
            if(buffer.size() > 1000) {
                flush();
            }
        } catch (Exception e) {
            errors.add("第 " + rowIndex + " 行：解析错误 - " + e.getMessage());
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    private void flush() {
        hrEmpSalaryInfoService.saveOrUpdateBatch(buffer);
        buffer.clear();
    }
}
