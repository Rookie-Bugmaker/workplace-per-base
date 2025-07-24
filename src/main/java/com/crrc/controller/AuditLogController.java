package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.AuditLog;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.AuditLogServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
* <p>
    * 日志表 前端控制器
    * </p>
*
* @author mayiyao
* @since 2025-07-24 09:07:55
*/
@RestController
@RequestMapping("/auditLog")
@RefreshScope
@Tag(name = "日志表", description = "日志表")
public class AuditLogController {

    @Autowired
    private AuditLogServiceImpl auditLogService;

    @PostMapping("/page")
    @Operation(summary = "获取日志表列表 page")
    @AuditLog(value = "获取日志表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageAuditLog(@Validated @RequestBody(required = false) AuditLog pageReqVO,
    @RequestParam(required = false, defaultValue = "1") int pageNo,
    @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new AuditLog() : pageReqVO;
        Page<AuditLog> page = new Page<>(pageNo, pageSize);
        auditLogService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取日志表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取日志表列表")
    @AuditLog(value = "获取日志表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<AuditLog>> queryAuditLog(@Validated @RequestBody(required = false) AuditLog queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new AuditLog() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取日志表列表成功", auditLogService.query(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得日志表实体")
    @AuditLog(value = "获得日志表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<AuditLog> getAuditLog(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取日志表实体成功", auditLogService.getById(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建日志表实体")
    @AuditLog(value = "创建日志表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createAuditLog(@Valid @RequestBody AuditLog createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建日志表实体成功", auditLogService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新日志表实体")
    @AuditLog(value = "更新日志表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateAuditLog(@Valid @RequestBody AuditLog updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新日志表实体成功", auditLogService.saveOrUpdate(updateReqVO));
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除日志表实体")
    @AuditLog(value = "删除日志表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteAuditLog(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除日志表实体成功", auditLogService.removeById(id));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出日志表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出日志表报表")
    public void exportAuditLog(@Validated @RequestBody(required = false) AuditLog exportReqVO,
    @RequestParam(required = false, defaultValue = "xlsx") String type,
    HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
            ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("日志表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
            .format(System.currentTimeMillis()), "UTF-8")
            .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new AuditLog() : exportReqVO;
        List<AuditLog> auditLogList = auditLogService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), AuditLog.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .autoCloseStream(true)
            .excelType(typeEnum)
            .sheet("日志表报表")
            .doWrite(auditLogList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入日志表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入日志表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), AuditLog.class,
        new ImportExcelListener<AuditLog, AuditLogServiceImpl>(auditLogService))
            .headRowNumber(1)
            .sheet()
            .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入日志表报表成功");
    }

}
