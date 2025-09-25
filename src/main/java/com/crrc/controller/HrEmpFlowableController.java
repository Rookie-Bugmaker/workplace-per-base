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
    public ResponseResult<WorkflowUserPageDTO> findUserByDeptId(Integer page, Integer pageSize, Long deptId,String roleCode) {
//        LoginUser curUser = SecurityFrameworkUtils.getLoginUser();
        page = 1;
        pageSize = 50000;
        log.info("获取人员审批信息：");
        LoginUser curUser = SecurityFrameworkUtils.getLoginUser();
        log.info("获取人员审批信息11：" + curUser);
        if (ObjectUtils.isEmpty(deptId)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "部门ID不能为空");
        }
        if (ObjectUtils.isEmpty(roleCode)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "roleCode不能为空");
        }
        WorkflowUserPageDTO workflowUserPage = new WorkflowUserPageDTO();
        if (ObjectUtils.isEmpty(page) || ObjectUtils.isEmpty(pageSize)) {
            //不需要分页查询
            page = 1;
            pageSize = 50000;
        }
        //根据部门ID查询部门人员列表
        ResultData<PaginationGrid<SysDeptPersonDTO>> resultData = sysServerClient.findPageByDeptId(deptId, page, pageSize);
        if (Boolean.FALSE.equals(resultData.getSuccess())) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "部门人员查询失败");
        }
        PaginationGrid<SysDeptPersonDTO> paginationGrid = resultData.getResult();
        if (ObjectUtils.isEmpty(paginationGrid)) {
            throw new IllegalArgumentException("查询失败");
        }
        List<SysDeptPersonDTO> deptPersonList = paginationGrid.getRows();
        if (CollectionUtils.isEmpty(deptPersonList)) {
            //没查询到数据，直接返回即可
            return ResponseResult.success(workflowUserPage);
        }
        List<Long> personIdList = deptPersonList.stream().map(SysDeptPersonDTO::getPersonId).collect(Collectors.toList());
        //根据人员ID查询用户信息
        ResultData<List<SysUserDTO>> userResultData = sysServerClient.findByPersonIds(personIdList);
        if (Boolean.FALSE.equals(userResultData.getSuccess())) {
            throw new IllegalArgumentException("用户查询失败");
        }
        List<SysUserDTO> userList = userResultData.getResult();
        if (CollectionUtils.isEmpty(userList)) {
            return ResponseResult.success(workflowUserPage);
        }
        for (SysUserDTO sysUser : userList) {
            SysDeptPersonDTO sysDeptPerson = deptPersonList.stream().filter(o -> o.getPersonId().equals(sysUser.getUserRelationId())).findFirst().orElse(null);
            if (!ObjectUtils.isEmpty(sysDeptPerson)) {
                sysUser.setFullName(sysDeptPerson.getPersonName());
            }
        }
        log.info("获取人员审批信息1：" + userList);
        // 获取角色下的用户
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCodes(Collections.singletonList(roleCode));
        ResultData<List<SysUserDTO>> result = sysServerClient.findUserByRoleCodes(sysUserRequestDTO);
        List<SysUserDTO> userByRoleCodes = result.getResult();
        if (ObjectUtils.isEmpty(userByRoleCodes)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "获取角色下的用户为空！");
        }
        log.info("获取人员审批信息2：" + result);
        // 获取当前部门用户和当前角色用户交集，返回审批人
        Set<Long> userIds = userByRoleCodes.stream()
                .map(SysUserDTO::getUserId)
                .collect(Collectors.toSet());
        List<SysUserDTO> finalUserList = userList.stream()
                .filter(user -> userIds.contains(user.getUserId()))
                .collect(Collectors.toList());
        log.info("获取人员审批信息3：" + finalUserList);
        if (ObjectUtils.isEmpty(finalUserList)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "查询的审批人为空，请配置审批人");
        }
        workflowUserPage.setTotal((long) finalUserList.size());
        workflowUserPage.setRows(finalUserList);
        return ResponseResult.success(workflowUserPage);
    }





}
