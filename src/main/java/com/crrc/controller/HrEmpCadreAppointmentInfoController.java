package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpCadreAppointmentInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.HrEmpCadreAppointmentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 中干任职信息表 前端控制器
 * </p>
 *
 * @author yukun
 * @since 2026-05-21
 */
@RestController
@RequestMapping("/hrEmpCadreAppointmentInfo")
@RefreshScope
@Tag(name = "中干任职信息表", description = "中干任职信息表")
public class HrEmpCadreAppointmentInfoController {

    @Autowired
    private HrEmpCadreAppointmentInfoService hrEmpCadreAppointmentInfoService;

    @GetMapping("/getDetails")
    @Operation(summary = "根据档案号获取中干任职信息详情")
    @AuditLog(value = "获取中干任职信息详情", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "userCode", description = "档案号", required = true, example = "013200012345")
    public ResponseResult<List<HrEmpCadreAppointmentInfo>> getDetails(@RequestParam String userCode) {
        List<HrEmpCadreAppointmentInfo> list = hrEmpCadreAppointmentInfoService.getDetails(userCode);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取中干任职信息详情成功", list);
    }

}
