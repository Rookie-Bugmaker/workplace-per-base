package com.crrc.controller;


import com.crrc.annotation.AuditLog;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.*;
import com.crrc.utils.DateValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.crrc.entity.*;

import java.time.LocalDate;
import java.util.*;

/**
 * @author mayiyao
 */
@Slf4j
@RestController
@RequestMapping("/hrTest")
public class TestController {

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;

    @PostMapping("/testLocalDate")
    @Operation(summary = "查询数据库中有没有非法的localDate值")
    @AuditLog(value = "同步人员分类，目前用定时任务每天处理", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<InvalidDateRecord>> syncPersonCategory(
            @RequestBody(required = false) List<HrEmpBasic> userCodeList) {

        // 1. 获取待检查数据（传参优先，否则全量）
        List<HrEmpBasic> list = (userCodeList != null && !userCodeList.isEmpty())
                ? hrEmpBasicService.getListByUserCode(userCodeList)
                : hrEmpBasicService.getAllUser();

        // 2. 检查并记录非法日期
        List<InvalidDateRecord> invalidRecords = new ArrayList<>();
        for (HrEmpBasic emp : list) {
            checkAndRecordInvalidDate(emp, "enlistmentDate", emp.getEnlistmentDate(), invalidRecords);
            checkAndRecordInvalidDate(emp, "promotionTime", emp.getPromotionTime(), invalidRecords);
            checkAndRecordInvalidDate(emp, "deathTime", emp.getDeathTime(), invalidRecords);
        }

        // 3. 返回结构化结果
        return new ResponseResult<>(HttpStatus.OK.value(),
                "检测完成，共发现 " + invalidRecords.size() + " 条非法日期记录",
                invalidRecords);
    }

    // 辅助方法：检查并记录非法日期
    private void checkAndRecordInvalidDate(HrEmpBasic emp, String fieldName, LocalDate date,
                                           List<InvalidDateRecord> records) {
        if (!DateValidator.isValidDate(date)) {
            records.add(new InvalidDateRecord(
                    emp.getUserCode(),  // 员工档案号
                    fieldName,          // 字段名
                    date                // 非法日期值
            ));
        }
    }

    // 记录非法日期的DTO
    @Data
    @AllArgsConstructor
    static class InvalidDateRecord {
        private String userCode;    // 员工档案号
        private String fieldName;   // 字段名（如enlistmentDate）
        private LocalDate invalidDate; // 非法日期值
    }
}