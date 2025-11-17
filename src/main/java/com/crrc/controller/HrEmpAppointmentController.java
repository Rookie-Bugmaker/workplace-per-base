package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.HrEmpAppointmentServiceImpl;
import com.crrc.utils.DeptUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任职信息 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpAppointment")
@RefreshScope
@Tag(name = "任职信息", description = "任职信息")
@Slf4j
public class HrEmpAppointmentController {

    @Autowired
    private HrEmpAppointmentServiceImpl hrEmpAppointmentService;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private WkDataAPi wkDataAPi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;

    @GetMapping("/isCadre")
    @Operation(summary = "检查是否中干")
    @AuditLog(value = "检查是否中干", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "userCode", description = "档案号", required = true, example = "013200012345")
    public ResponseResult<Boolean> isCadre(@RequestParam String userCode) {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HrEmpBasic::getUserCode, userCode);
        List<HrEmpBasic> basicList = hrEmpBasicMapper.selectList(queryWrapper);
        if(ObjectUtils.isEmpty(basicList)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "OK", false);
        }

        Set<String> cadreSet = new HashSet<>(Arrays.asList(
                "004", // 中
                "005", // 干业
                "006", // 人业
                "中",
                "干业",
                "人业"
        ));

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", cadreSet.contains(basicList.get(0).getCategory4()));
    }

    @GetMapping("/getCadrePosition")
    @Operation(summary = "获取最新的中干任职信息")
    @AuditLog(value = "获取最新的中干任职信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "userCode", description = "档案号", required = true, example = "013200012345")
    public ResponseResult<List<HrEmpAppointment>> getCadrePosition(@RequestParam String userCode) {
        // 获取最新的中干任职信息
        return new ResponseResult<>(HttpStatus.OK.value(), "OK", hrEmpAppointmentService.selectLatestMsg(Collections.singletonList(userCode)));
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得任职信息实体")
    @AuditLog(value = "获得任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpAppointment>> getMsg(@RequestParam String userCode) {
        List<HrEmpAppointment> list = hrEmpAppointmentService.selectMsg(userCode);
        // 回显码值，当数据为数字开头的时候才会进行转写，否则直接返回
        for(HrEmpAppointment entity : list) {
            String deptCode = entity.getDept();
            String deptName = DeptUtils.getDeptNameByCode(deptCode);
            entity.setDept(deptName);

            // 处理组织单元
            String orgId = entity.getOrgUnit();
            String orgName = DeptUtils.getDeptNameByCode(orgId);
            entity.setOrgUnit(orgName);

            // 处理组织
            String orgOriginal = entity.getOrg();
            if (StringUtils.isNotBlank(orgOriginal)) {
                String org = orgOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(orgOriginal, "hr_emp_appointment", "APPOINTMENT_ORG") : orgOriginal;
                entity.setOrg(org);
            }

            // 处理职位名称
            String position = entity.getPosition();
            if (StringUtils.isNotBlank(position)) {
                String positionName = position.matches("^\\d.*") ?
                        codeInfoService.translateCode(position, "hr_emp_appointment", "APPOINTMENT_POSITION") : position;
                entity.setPosition(positionName);
            }

            // 处理变动事件 changeEvent
            String changeEventOriginal = entity.getChangeEvent();
            if (StringUtils.isNotBlank(changeEventOriginal)) {
                String changeEvent = changeEventOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeEventOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_EVENT") : changeEventOriginal;
                entity.setChangeEvent(changeEvent);
            }

            // 处理变动类型 changeType
            String changeTypeOriginal = entity.getChangeType();
            if (StringUtils.isNotBlank(changeTypeOriginal)) {
                String changeType = changeTypeOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeTypeOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE") : changeTypeOriginal;
                entity.setChangeType(changeType);
            }

            // 处理变动原因 changeReason
            String changeReasonOriginal = entity.getChangeReason();
            if (StringUtils.isNotBlank(changeReasonOriginal)) {
                String changeReason = changeReasonOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeReasonOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_REASON") : changeReasonOriginal;
                entity.setChangeReason(changeReason);
            }

            // 处理在岗状态 dutyStatus
            String dutyStatusOriginal = entity.getDutyStatus();
            if (StringUtils.isNotBlank(dutyStatusOriginal)) {
                String dutyStatus = dutyStatusOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(dutyStatusOriginal, "hr_emp_appointment", "APPOINTMENT_DUTY_STATUS") : dutyStatusOriginal;
                entity.setDutyStatus(dutyStatus);
            }

            // 处理职务类别 positionCategory
            String positionCategoryOriginal = entity.getPositionCategory();
            if (StringUtils.isNotBlank(positionCategoryOriginal)) {
                String positionCategory = positionCategoryOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(positionCategoryOriginal, "hr_emp_appointment", "APPOINTMENT_POSITION_CATEGORY") : positionCategoryOriginal;
                entity.setPositionCategory(positionCategory);
            }

        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工任职信息表实体成功", list);
    }

    @PostMapping("/getLatestDetailList")
    @Operation(summary = "获得任职信息实体")
    @AuditLog(value = "获得任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpAppointment>> getLatestDetailList(@RequestBody List<String> userCodeList) {
        List<HrEmpAppointment> resultList = hrEmpAppointmentService.selectLatestMsg(userCodeList);
        Map<String, String> translateMap = codeInfoService.getTranslateMap("hr_emp_appointment", "APPOINTMENT_POSITION");
        Map<String, String> code2NameMap = wkDataAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));
        // 回显部门名称、组织单元名称、职位名称
        for(HrEmpAppointment entity : resultList) {
            entity.setDept(code2NameMap.getOrDefault(entity.getDept(), null));

            entity.setOrgUnit(code2NameMap.getOrDefault(entity.getOrgUnit(), null));

            entity.setPosition(translateMap.getOrDefault(entity.getPosition(), "未知职务"));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工任职信息表实体成功", resultList);
    }

    @PostMapping("/page")
    @Operation(summary = "获取任职信息列表 page")
    @AuditLog(value = "获取任职信息列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpAppointment(@Validated @RequestBody(required = false) HrEmpAppointment pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpAppointment() : pageReqVO;
        Page<HrEmpAppointment> page = new Page<>(pageNo, pageSize);
        hrEmpAppointmentService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取任职信息列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取任职信息列表")
    @AuditLog(value = "获取任职信息列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpAppointment>> queryHrEmpAppointment(@Validated @RequestBody(required = false) HrEmpAppointment queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpAppointment() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取任职信息列表成功", hrEmpAppointmentService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得任职信息实体")
    @AuditLog(value = "获得任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpAppointment> getHrEmpAppointment(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取任职信息实体成功", hrEmpAppointmentService.getById(id));
    }


    @PostMapping("/create")
    @Operation(summary = "创建任职信息实体")
    @AuditLog(value = "创建任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> createHrEmpAppointment(@Valid @RequestBody HrEmpAppointment createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建任职信息实体成功", hrEmpAppointmentService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新任职信息实体")
    @AuditLog(value = "更新任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpAppointment(@Valid @RequestBody HrEmpAppointment updateReqVO) {
        if(updateReqVO.getId() == null){
            updateReqVO.setId(IdUtil.simpleUUID());
            return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpAppointmentService.save(updateReqVO));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新任职信息实体成功", hrEmpAppointmentService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除任职信息实体")
    @AuditLog(value = "删除任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpAppointment(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除任职信息实体成功", hrEmpAppointmentService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出任职信息报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出任职信息报表")
    public void exportHrEmpAppointment(@Validated @RequestBody(required = false) HrEmpAppointment exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("任职信息报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpAppointment() : exportReqVO;
        List<HrEmpAppointment> hrEmpAppointmentList = hrEmpAppointmentService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpAppointment.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("任职信息报表")
                .doWrite(hrEmpAppointmentList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入任职信息报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入任职信息报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpAppointment.class,
                new ImportExcelListener<HrEmpAppointment, HrEmpAppointmentServiceImpl>(hrEmpAppointmentService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入任职信息报表成功");
    }

}
