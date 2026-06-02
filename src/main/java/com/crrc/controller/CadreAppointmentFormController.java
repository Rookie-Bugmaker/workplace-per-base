package com.crrc.controller;

import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CadreAppointmentFormGenerateServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 干部任免审批表
 */
@RestController
@RequestMapping("/cadreAppointmentForm")
@Tag(name = "干部任免审批表")
public class CadreAppointmentFormController {

    @Autowired
    private CadreAppointmentFormGenerateServiceImpl cadreAppointmentFormGenerateService;

    /**
     * 根据档案号(userCode)生成干部任免审批表，支持一个或多个。
     * 返回每个档案号的生成结果：{userCode, fileId} 或 {userCode, error}。
     */
    @PostMapping("/generate")
    @Operation(summary = "根据档案号生成干部任免审批表（支持批量）")
    public ResponseResult<List<Map<String, String>>> generate(@RequestBody List<String> userCodes) {
        if (userCodes == null || userCodes.isEmpty()) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "档案号不能为空");
        }
        List<Map<String, String>> data = cadreAppointmentFormGenerateService.generateForUserCodes(userCodes);
        return new ResponseResult<>(HttpStatus.OK.value(), "生成成功", data);
    }
}
