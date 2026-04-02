package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpOtherAppointment;
import com.crrc.entity.PositionInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.HrEmpOtherAppointmentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 其他任职信息 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpOtherAppointment")
@RefreshScope
@Tag(name = "其他任职信息", description = "其他任职信息")
public class HrEmpOtherAppointmentController {

    @Autowired
    private HrEmpOtherAppointmentServiceImpl hrEmpOtherAppointmentService;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private PositionInfoMapper positionInfoMapper;

    @GetMapping("/getDetails")
    @Operation(summary = "获得其他任职信息实体")
    @AuditLog(value = "获得其他任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpOtherAppointment>> getMsg(@RequestParam String userCode ) {
        List<HrEmpOtherAppointment> list = hrEmpOtherAppointmentService.selectMsg(userCode);
        // 回显码值，当数据为数字开头的时候才会进行转写，否则直接返回
        for(HrEmpOtherAppointment entity : list) {
            // 处理组织
            String originDeptCode = entity.getDept();
            if (StringUtils.isNotBlank(originDeptCode)) {
                String deptCode = originDeptCode.matches("^\\d.*") ?
                        codeInfoService.translateCode(originDeptCode, "hr_emp_appointment", "APPOINTMENT_ORG") : originDeptCode;
                entity.setDept(deptCode);
            }

            // 处理组织单元
            String originOrgUnit = entity.getOrgUnit();
            if (StringUtils.isNotBlank(originOrgUnit)) {
                String orgUnit = originOrgUnit.matches("^\\d.*") ?
                        codeInfoService.translateCode(originOrgUnit, "hr_emp_appointment", "APPOINTMENT_ORG") : originOrgUnit;
                entity.setOrgUnit(orgUnit);
            }

            // 处理组织
            String orgOriginal = entity.getOrg();
            if (StringUtils.isNotBlank(orgOriginal)) {
                String org = orgOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(orgOriginal, "hr_emp_appointment", "APPOINTMENT_ORG") : orgOriginal;
                entity.setOrg(org);
            }

            // 处理职位名称
            String position = entity.getPosition();
            if (StringUtils.isNotBlank(position) && position.matches("^\\d.*")) {
                LambdaQueryWrapper<PositionInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PositionInfo::getCode, position);
                List<PositionInfo> positionInfos = positionInfoMapper.selectList(wrapper);
                if(!positionInfos.isEmpty()) {
                    String positionName = positionInfos.get(0).getName();
                    entity.setPosition(positionName);
                }
            }

            // 处理其他任职类型
            String otherAppointmentType = entity.getOtherAppointmentType();
            if (StringUtils.isNotBlank(otherAppointmentType)) {
                String otherAppointmentTypeValue = otherAppointmentType.matches("^\\d.*") ?
                        codeInfoService.translateCode(otherAppointmentType, "hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE") : otherAppointmentType;
                entity.setOtherAppointmentType(otherAppointmentTypeValue);
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

            // 处理结束时间，如果是2198年以后的，直接置为空
            if(entity.getEndDate() != null && entity.getEndDate().isAfter(LocalDate.parse("2198-01-01"))) {
                entity.setEndDate(null);
            }
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取其他任职信息成功", list);
    }

    @PostMapping("/page")
    @Operation(summary = "获取其他任职信息列表 page")
    @AuditLog(value = "获取其他任职信息列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage> pageHrEmpOtherAppointment(@Validated @RequestBody(required = false) HrEmpOtherAppointment pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpOtherAppointment() : pageReqVO;
        Page<HrEmpOtherAppointment> page = new Page<>(pageNo, pageSize);
        hrEmpOtherAppointmentService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取其他任职信息列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取其他任职信息列表")
    @AuditLog(value = "获取其他任职信息列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpOtherAppointment>> queryHrEmpOtherAppointment(@Validated @RequestBody(required = false) HrEmpOtherAppointment queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpOtherAppointment() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取其他任职信息列表成功", hrEmpOtherAppointmentService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得其他任职信息实体")
    @AuditLog(value = "获得其他任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpOtherAppointment> getHrEmpOtherAppointment(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取其他任职信息实体成功", hrEmpOtherAppointmentService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建其他任职信息实体")
    @AuditLog(value = "创建其他任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpOtherAppointment(@Valid @RequestBody HrEmpOtherAppointment createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建其他任职信息实体成功", hrEmpOtherAppointmentService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新其他任职信息实体")
    @AuditLog(value = "更新其他任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpOtherAppointment(@Valid @RequestBody HrEmpOtherAppointment updateReqVO) {
        if(updateReqVO.getId() == null){
            updateReqVO.setId(IdUtil.simpleUUID());
            return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpOtherAppointmentService.save(updateReqVO));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新其他任职信息实体成功", hrEmpOtherAppointmentService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除其他任职信息实体")
    @AuditLog(value = "删除其他任职信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpOtherAppointment(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除其他任职信息实体成功", hrEmpOtherAppointmentService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出其他任职信息报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出其他任职信息报表")
    public void exportHrEmpOtherAppointment(@Validated @RequestBody(required = false) HrEmpOtherAppointment exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("其他任职信息报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpOtherAppointment() : exportReqVO;
        List<HrEmpOtherAppointment> hrEmpOtherAppointmentList = hrEmpOtherAppointmentService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpOtherAppointment.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("其他任职信息报表")
                .doWrite(hrEmpOtherAppointmentList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入其他任职信息报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入其他任职信息报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpOtherAppointment.class,
                new ImportExcelListener<HrEmpOtherAppointment, HrEmpOtherAppointmentServiceImpl>(hrEmpOtherAppointmentService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入其他任职信息报表成功");
    }

}
