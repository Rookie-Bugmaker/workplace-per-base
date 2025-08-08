package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpSalaryStandard;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpSalaryStandardServiceImpl;
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
 * 薪资标准 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpSalaryStandard")
@RefreshScope
@Tag(name = "薪资标准", description = "薪资标准")
public class HrEmpSalaryStandardController {

    @Autowired
    private HrEmpSalaryStandardServiceImpl hrEmpSalaryStandardService;

    @GetMapping("/getDetails")
    @Operation(summary = "获得薪资标准实体")
    @AuditLog(value = "获得薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpSalaryStandard>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表成功", hrEmpSalaryStandardService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取薪资标准列表 page")
    @AuditLog(value = "获取薪资标准列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpSalaryStandard() : pageReqVO;
        Page<HrEmpSalaryStandard> page = new Page<>(pageNo, pageSize);
        hrEmpSalaryStandardService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取薪资标准列表")
    @AuditLog(value = "获取薪资标准列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpSalaryStandard>> queryHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpSalaryStandard() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表成功", hrEmpSalaryStandardService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得薪资标准实体")
    @AuditLog(value = "获得薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpSalaryStandard> getHrEmpSalaryStandard(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准实体成功", hrEmpSalaryStandardService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建薪资标准实体")
    @AuditLog(value = "创建薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpSalaryStandard(@Valid @RequestBody HrEmpSalaryStandard createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建薪资标准实体成功", hrEmpSalaryStandardService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新薪资标准实体")
    @AuditLog(value = "更新薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpSalaryStandard(@Valid @RequestBody HrEmpSalaryStandard updateReqVO) {
        if(updateReqVO.getId() == null){
            updateReqVO.setId(IdUtil.simpleUUID());
            return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpSalaryStandardService.save(updateReqVO));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新薪资标准实体成功", hrEmpSalaryStandardService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除薪资标准实体")
    @AuditLog(value = "删除薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpSalaryStandard(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除薪资标准实体成功", hrEmpSalaryStandardService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出薪资标准报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出薪资标准报表")
    public void exportHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("薪资标准报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpSalaryStandard() : exportReqVO;
        List<HrEmpSalaryStandard> hrEmpSalaryStandardList = hrEmpSalaryStandardService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpSalaryStandard.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("薪资标准报表")
                .doWrite(hrEmpSalaryStandardList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入薪资标准报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入薪资标准报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpSalaryStandard.class,
                new ImportExcelListener<HrEmpSalaryStandard, HrEmpSalaryStandardServiceImpl>(hrEmpSalaryStandardService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入薪资标准报表成功");
    }

}
