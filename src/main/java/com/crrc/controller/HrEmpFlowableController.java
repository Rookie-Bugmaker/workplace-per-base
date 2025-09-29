package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.client.SysServerClient;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpHealth;
import com.crrc.entity.HrEmpHouseHold;
import com.crrc.entity.LoginUser;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.*;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.service.impl.HrEmpHealthServiceImpl;
import com.crrc.service.impl.HrEmpHouseholdServiceImpl;
import com.crrc.utils.SecurityFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 获取审批人信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Slf4j
@RestController
@RequestMapping("/empFlowable")
@RefreshScope
@Tag(name = "获取审批人信息", description = "获取审批人信息")
public class HrEmpFlowableController {

    @Resource
    private SysServerClient sysServerClient;


    @GetMapping("/findUserByDeptIdAndRoleCode")
    public ResponseResult<List<SysUserDTO>> findUserByDeptIdAndRoleCode(Long companyId, Long deptId, String roleCode) {
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode 查询参数companyId:{},deptId:{},roleCode:{}", companyId, deptId, roleCode);
        if (ObjectUtils.isEmpty(deptId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "deptId不能为空");
        }
        if (ObjectUtils.isEmpty(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }
        if (ObjectUtils.isEmpty(companyId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "companyId不能为空");
        }
        // 查询人员信息
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCode(roleCode);
        sysUserRequestDTO.setCompanyId(companyId);
        sysUserRequestDTO.setDeptId(deptId);
        ResultData<List<SysUserDTO>> userByRoleCodeAndCompanyId = sysServerClient.findUserByRoleCode(sysUserRequestDTO);
        log.info("触发人力资源获取人员信息接口findUserByDeptIdAndRoleCode {}", userByRoleCodeAndCompanyId.getResult());
        if(ObjectUtils.isEmpty(userByRoleCodeAndCompanyId) || ObjectUtils.isEmpty(userByRoleCodeAndCompanyId.getResult())){
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "未查到审批人数据!");
        }
        List<SysUserDTO> result = userByRoleCodeAndCompanyId.getResult();
        return ResponseResult.success(result);
    }




}
