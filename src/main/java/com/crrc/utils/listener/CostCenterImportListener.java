package com.crrc.utils.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CostCenterImportListener implements ReadListener<HrEmpBasic> {

    private final List<String> errors = new ArrayList<>();

    HrEmpBasicServiceImpl hrEmpBasicService;

    List<HrEmpBasic> buffer = new ArrayList<>();

    private int rowIndex = 0;

    public CostCenterImportListener(HrEmpBasicServiceImpl hrEmpBasicService) {
        this.hrEmpBasicService = hrEmpBasicService;
    }

    @Override
    public void invoke(HrEmpBasic hrEmpBasic, AnalysisContext analysisContext) {

        try{
            rowIndex++;
            // 必填项校验
            if (!StringUtils.hasText(hrEmpBasic.getUserCode())) {
                errors.add("第 " + rowIndex + "行，存在空档案号，请检查excel");
                return;
            }

            List<String> userCodeList = new ArrayList<>();
            userCodeList.add(hrEmpBasic.getUserCode());

            List<HrEmpBasic> existMsgList = hrEmpBasicService.getListByUserCode(userCodeList);
            if(existMsgList != null && !existMsgList.isEmpty()) {
                existMsgList.get(0).setCostCenter(hrEmpBasic.getCostCenter());
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
        hrEmpBasicService.saveOrUpdateBatch(buffer);
        buffer.clear();
    }
}
