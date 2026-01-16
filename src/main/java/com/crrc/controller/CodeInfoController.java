package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.CostCenter;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.fasterxml.uuid.Generators;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/codeInfo")
@RefreshScope
public class CodeInfoController {
    @Autowired
    CodeInfoServiceImpl codeInfoService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateCodeInfo(@Valid @RequestBody CodeInfo updateReqVO) {
        if(updateReqVO.getId() == null) {
            updateReqVO.setId(Generators.timeBasedEpochGenerator().generate().toString());
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新实体成功", codeInfoService.saveOrUpdate(updateReqVO));
    }
}
