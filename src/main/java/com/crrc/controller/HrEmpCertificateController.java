package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpCertificate;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpCertificateServiceImpl;
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
 * 员工证件信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpCertificate")
@RefreshScope
@Tag(name = "员工证件信息表", description = "员工证件信息表")
public class HrEmpCertificateController {

    @Autowired
    private HrEmpCertificateServiceImpl hrEmpCertificateService;

    @PostMapping("/page")
    @Operation(summary = "获取员工证件信息表列表 page")
    @AuditLog(value = "获取员工证件信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpCertificate(@Validated @RequestBody(required = false) HrEmpCertificate pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpCertificate() : pageReqVO;
        Page<HrEmpCertificate> page = new Page<>(pageNo, pageSize);
        hrEmpCertificateService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工证件信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工证件信息表列表")
    @AuditLog(value = "获取员工证件信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpCertificate>> queryHrEmpCertificate(@Validated @RequestBody(required = false) HrEmpCertificate queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpCertificate() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工证件信息表列表成功", hrEmpCertificateService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工证件信息表实体")
    @AuditLog(value = "获得员工证件信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpCertificate> getHrEmpCertificate(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工证件信息表实体成功", hrEmpCertificateService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工证件信息表实体")
    @AuditLog(value = "创建员工证件信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpCertificate(@Valid @RequestBody HrEmpCertificate createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工证件信息表实体成功", hrEmpCertificateService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工证件信息表实体")
    @AuditLog(value = "更新员工证件信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpCertificate(@Valid @RequestBody HrEmpCertificate updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工证件信息表实体成功", hrEmpCertificateService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工证件信息表实体")
    @AuditLog(value = "删除员工证件信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpCertificate(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工证件信息表实体成功", hrEmpCertificateService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工证件信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工证件信息表报表")
    public void exportHrEmpCertificate(@Validated @RequestBody(required = false) HrEmpCertificate exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工证件信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpCertificate() : exportReqVO;
        List<HrEmpCertificate> hrEmpCertificateList = hrEmpCertificateService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpCertificate.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工证件信息表报表")
                .doWrite(hrEmpCertificateList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工证件信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工证件信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpCertificate.class,
                new ImportExcelListener<HrEmpCertificate, HrEmpCertificateServiceImpl>(hrEmpCertificateService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工证件信息表报表成功");
    }

}
