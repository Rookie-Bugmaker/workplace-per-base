package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.PostCatalogInfoImportResultVO;
import com.crrc.pojo.vo.PostCatalogInfoImportVO;
import com.crrc.pojo.vo.PostCatalogInfoTreeVO;
import com.crrc.service.impl.PostCatalogInfoServiceImpl;
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
import java.util.Collections;
import java.util.List;

/**
 * @author yukun
 */
@RestController
@RequestMapping("/postCatalogInfo")
@RefreshScope
public class PostCatalogInfoController {
    @Autowired
    private PostCatalogInfoServiceImpl postCatalogInfoService;

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pagePostCatalogInfo(@Validated @RequestBody(required = false) PostCatalogInfo pageReqVO,
                                                     @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                     @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PostCatalogInfo() : pageReqVO;
        Page<PostCatalogInfo> page = new Page<>(pageNo, pageSize);
        postCatalogInfoService.page(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<PostCatalogInfo>> queryPostCatalogInfo(@Validated @RequestBody(required = false) PostCatalogInfo queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PostCatalogInfo() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", postCatalogInfoService.query(queryReqVO));
    }

    @PostMapping("/queryTree")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<PostCatalogInfoTreeVO>> queryPostCatalogInfoTree() {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", postCatalogInfoService.queryTree());
    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PostCatalogInfo> getPostCatalogInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", postCatalogInfoService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createPostCatalogInfo(@Valid @RequestBody PostCatalogInfo createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", postCatalogInfoService.saveWithValidation(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updatePostCatalogInfo(@Valid @RequestBody PostCatalogInfo updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新实体成功", postCatalogInfoService.updateWithValidation(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePostCatalogInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除/停用实体成功", postCatalogInfoService.removeOrDisableById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/disable")
    @Operation(summary = "停用实体")
    @AuditLog(value = "停用实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> disablePostCatalogInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "停用实体成功", postCatalogInfoService.disableById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/enable")
    @Operation(summary = "启用实体")
    @AuditLog(value = "启用实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> enablePostCatalogInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "启用实体成功", postCatalogInfoService.enableById(id));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportPostCatalogInfo(@Validated @RequestBody(required = false) PostCatalogInfo exportReqVO,
                                      @RequestParam(required = false, defaultValue = "xlsx") String type,
                                      HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("树形职务族报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new PostCatalogInfo() : exportReqVO;
        List<PostCatalogInfo> postCatalogInfoList = postCatalogInfoService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PostCatalogInfo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("树形职务族报表")
                .doWrite(postCatalogInfoList);
    }

    @PostMapping("/exportTemplate")
    @AuditLog(value = "导出树形职务族导入模板", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出树形职务族导入模板")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("树形职务族导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PostCatalogInfoImportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("ccar_job_family_tree")
                .doWrite(Collections.emptyList());
    }

    @PostMapping("/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws Exception {
        postCatalogInfoService.importFull(file);
        return new ResponseResult<>(HttpStatus.OK.value(), "导入报表成功", true);
    }

    @PostMapping("/importFull")
    @AuditLog(value = "完整导入树形职务族", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "完整导入树形职务族")
    public ResponseResult<PostCatalogInfoImportResultVO> importFull(MultipartFile file) throws Exception {
        return new ResponseResult<>(HttpStatus.OK.value(), "完整导入树形职务族成功", postCatalogInfoService.importFull(file));
    }
}
