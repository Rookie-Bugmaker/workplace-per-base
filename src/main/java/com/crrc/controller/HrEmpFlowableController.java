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

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;

    @Resource
    private SysServerClient sysServerClient;





    @GetMapping("/findUserByDeptIdAndRoleCode")
    public ResponseResult<WorkflowUserPageDTO> findUserByDeptId(Integer page, Integer pageSize, Long deptId,String roleCode) {
//        LoginUser curUser = SecurityFrameworkUtils.getLoginUser();
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
            pageSize = 10000;
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
        // 获取角色下的用户
        SysUserRequestDTO sysUserRequestDTO = new SysUserRequestDTO();
        sysUserRequestDTO.setRoleCodes(Collections.singletonList(roleCode));
        ResultData<List<SysUserDTO>> result = sysServerClient.findUserByRoleCodes(sysUserRequestDTO);
        List<SysUserDTO> userByRoleCodes = result.getResult();
        if (ObjectUtils.isEmpty(userByRoleCodes)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "获取角色下的用户为空！");
        }
        // 获取当前部门用户和当前角色用户交集，返回审批人
        Set<Long> userIds = userByRoleCodes.stream()
                .map(SysUserDTO::getUserId)
                .collect(Collectors.toSet());
        List<SysUserDTO> finalUserList = userList.stream()
                .filter(user -> userIds.contains(user.getUserId()))
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(finalUserList)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "查询的审批人为空，请配置审批人");
        }
        workflowUserPage.setTotal((long) finalUserList.size());
        workflowUserPage.setRows(finalUserList);
        return ResponseResult.success(workflowUserPage);
    }






    @GetMapping("/getDetails")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsg(@RequestParam String userCode) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工基本信息表列表 page")
    @AuditLog(value = "获取员工基本信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<HrEmpBasicDTO>> pageHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasicSearchDTO pageReqVO,
                                                               @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                               @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpBasicSearchDTO() : pageReqVO;
        try {
            Page<HrEmpBasicDTO> page = new Page<>(pageNo, pageSize);
            IPage<HrEmpBasicDTO> result = hrEmpBasicService.listData(page, pageReqVO);
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工基本信息表列表")
    @AuditLog(value = "获取员工基本信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpBasic>> queryHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasic queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpBasic() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表列表成功", hrEmpBasicService.query(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpBasic> getHrEmpBasic(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工基本信息表实体")
    @AuditLog(value = "创建员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpBasic(@Valid @RequestBody HrEmpBasic createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpBasicService.save(createReqVO));
    }


    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工基本信息表实体")
    @AuditLog(value = "删除员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpBasic(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工基本信息表实体成功", hrEmpBasicService.removeById(id));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出员工基本信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工基本信息表报表")
    public void exportHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasic exportReqVO,
                                 @RequestParam(required = false, defaultValue = "xlsx") String type,
                                 HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工基本信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        exportReqVO = (exportReqVO == null) ? new HrEmpBasic() : exportReqVO;
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpBasic.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工基本信息表报表")
                .doWrite(hrEmpBasicList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工基本信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工基本信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpBasic.class,
                        new ImportExcelListener<HrEmpBasic, HrEmpBasicServiceImpl>(hrEmpBasicService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工基本信息表报表成功");
    }

}
