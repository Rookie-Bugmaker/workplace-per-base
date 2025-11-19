package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.api.WkCoreAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.DeptqueryDTO;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.dto.DeptReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/common")
@RefreshScope
public class CommonController {
    @Autowired
    WkCoreAPi wkCoreAPi;
    @Qualifier("com.crrc.client.SysServerClient")
    @Autowired
    private SysServerClient sysServerClient;

    @GetMapping("/dept/getList")
    @Operation(summary = "获取全量部门信息")
    @AuditLog(value = "获取全量部门信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResultDTO<List<DeptDTO>> getDeptList() {
        ResultData<List<DeptDTO>> deptDTOResultData = sysServerClient.listDeptVO(new DeptReqDTO());
        if(deptDTOResultData == null || deptDTOResultData.getResult() == null) {
            return new ResponseResultDTO<>(HttpStatus.BAD_REQUEST.value(), "获取部门错误，框架接口出错", null);
        }
        return new ResponseResultDTO<>(HttpStatus.OK.value(), "获取部门成功", deptDTOResultData.getResult());
    }

}
