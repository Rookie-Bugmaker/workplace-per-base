package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.PositionJobGrade;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.PositionJobGradeServiceImpl;
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
 * 职位-岗级/标准职级表 前端控制器
 * </p>
 *
 * @author yukun
 * @since 2025-12-19 08:35:16
 */
@RestController
@RequestMapping("/positionJobGrade")
@RefreshScope
@Tag(name = "职位-岗级/标准职级表", description = "职位-岗级/标准职级表")
public class PositionJobGradeController {

    private final PositionJobGradeServiceImpl positionJobGradeService;

    @Autowired
    public PositionJobGradeController(PositionJobGradeServiceImpl positionJobGradeService) {
        this.positionJobGradeService = positionJobGradeService;
    }

    @PostMapping("/page")
    @Operation(summary = "获取职位-岗级/标准职级表列表 page")
    @AuditLog(value = "获取职位-岗级/标准职级表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage<PositionJobGrade>> pagePositionJobGrade(@Validated @RequestBody(required = false) PositionJobGrade pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PositionJobGrade() : pageReqVO;
        Page<PositionJobGrade> page = new Page<>(pageNo, pageSize);
        positionJobGradeService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职位-岗级/标准职级表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取职位-岗级/标准职级表列表")
    @AuditLog(value = "获取职位-岗级/标准职级表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<PositionJobGrade>> queryPositionJobGrade(@Validated @RequestBody(required = false) PositionJobGrade queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PositionJobGrade() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职位-岗级/标准职级表列表成功", positionJobGradeService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得职位-岗级/标准职级表实体")
    @AuditLog(value = "获得职位-岗级/标准职级表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PositionJobGrade> getPositionJobGrade(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职位-岗级/标准职级表实体成功", positionJobGradeService.getById(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建职位-岗级/标准职级表实体")
    @AuditLog(value = "创建职位-岗级/标准职级表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createPositionJobGrade(@Valid @RequestBody PositionJobGrade createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建职位-岗级/标准职级表实体成功", positionJobGradeService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新职位-岗级/标准职级表实体")
    @AuditLog(value = "更新职位-岗级/标准职级表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updatePositionJobGrade(@Valid @RequestBody PositionJobGrade updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新职位-岗级/标准职级表实体成功", positionJobGradeService.saveOrUpdate(updateReqVO));
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除职位-岗级/标准职级表实体")
    @AuditLog(value = "删除职位-岗级/标准职级表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositionJobGrade(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除职位-岗级/标准职级表实体成功", positionJobGradeService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出职位-岗级/标准职级表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出职位-岗级/标准职级表报表")
    public void exportPositionJobGrade(@Validated @RequestBody(required = false) PositionJobGrade exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("职位-岗级/标准职级表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new PositionJobGrade() : exportReqVO;
        List<PositionJobGrade> positionJobGradeList = positionJobGradeService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PositionJobGrade.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("职位-岗级/标准职级表报表")
                .doWrite(positionJobGradeList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入职位-岗级/标准职级表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入职位-岗级/标准职级表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PositionJobGrade.class,
                new ImportExcelListener<PositionJobGrade, PositionJobGradeServiceImpl>(positionJobGradeService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入职位-岗级/标准职级表报表成功");
    }

}
