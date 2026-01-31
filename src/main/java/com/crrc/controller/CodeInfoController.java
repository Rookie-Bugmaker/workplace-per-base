package com.crrc.controller;

import cn.hutool.core.util.IdUtil;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CodeInfoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mayiyao
 */
@Slf4j
@RestController
@RequestMapping("/codeInfo")
public class CodeInfoController {

    private final CodeInfoServiceImpl codeInfoService;
    private final WkDataAPi wkDataAPi;

    @Autowired
    public CodeInfoController(CodeInfoServiceImpl codeInfoService, WkDataAPi wkDataAPi) {
        this.codeInfoService = codeInfoService;
        this.wkDataAPi = wkDataAPi;
    }

    @GetMapping("/syncOrgInfo")
    @Operation(summary = "同步组织数据（增量）")
    @AuditLog(value = "同步组织数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    public ResponseResult<Boolean> syncOrgInfo() {
        List<OrganizationDTO> dtoList = wkDataAPi.allOrganization().getData();
        Map<String, String> newMap = dtoList.stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName,
                (a, b) -> a
        ));

        Map<String, String> oldMap = codeInfoService.getTranslateMap("hr_emp_appointment", "APPOINTMENT_ORG");

        List<CodeInfo> saveList = newMap.entrySet().stream().filter(
                k -> !oldMap.containsKey(k.getKey())
        ).map(e -> {
            CodeInfo codeInfo = new CodeInfo();
            codeInfo.setId(IdUtil.randomUUID());
            codeInfo.setCode(e.getKey());
            codeInfo.setCodeValue(e.getValue());
            codeInfo.setType("APPOINTMENT_ORG");
            codeInfo.setTypeName("组织");
            codeInfo.setTableName("hr_emp_appointment");
            return codeInfo;
        }).collect(Collectors.toList());

        // 增加到codeInfo中
        codeInfoService.saveBatch(saveList);

        return new ResponseResult<>(HttpStatus.OK.value(), "OK");
    }

}
