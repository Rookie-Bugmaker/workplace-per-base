package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrOrgCostCenter;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrOrgCostCenterServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.List;

/**
 *
 * @author why
 * @since 2025-08-26 08:51:30
 */
@RestController
@RequestMapping("/hrOrgCostCenter")
@RefreshScope
@Deprecated
public class HrOrgCostCenterController {

    @Autowired
    private HrOrgCostCenterServiceImpl hrOrgCostCenterService;

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrOrgCostCenter(@Validated @RequestBody(required = false) HrOrgCostCenter pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrOrgCostCenter() : pageReqVO;
        Page<HrOrgCostCenter> page = new Page<>(pageNo, pageSize);
        hrOrgCostCenterService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrOrgCostCenter>> queryHrOrgCostCenter(@Validated @RequestBody(required = false) HrOrgCostCenter queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrOrgCostCenter() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", hrOrgCostCenterService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrOrgCostCenter> getHrOrgCostCenter(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", hrOrgCostCenterService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrOrgCostCenter(@Valid @RequestBody HrOrgCostCenter createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", hrOrgCostCenterService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrOrgCostCenter(@Valid @RequestBody HrOrgCostCenter updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新实体成功", hrOrgCostCenterService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrOrgCostCenter(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", hrOrgCostCenterService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportHrOrgCostCenter(@Validated @RequestBody(required = false) HrOrgCostCenter exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrOrgCostCenter() : exportReqVO;
        List<HrOrgCostCenter> hrOrgCostCenterList = hrOrgCostCenterService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrOrgCostCenter.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(hrOrgCostCenterList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrOrgCostCenter.class,
                new ImportExcelListener<HrOrgCostCenter, HrOrgCostCenterServiceImpl>(hrOrgCostCenterService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入报表成功");
    }

}
