package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.PositionInfo;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowUserPageDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.PositionInfoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author yukun
 */
@RestController
@RequestMapping("/positionInfo")
@RefreshScope
@Slf4j
public class PositionInfoController {
    private final PositionInfoServiceImpl positionInfoService;

    @Autowired
    public PositionInfoController(PositionInfoServiceImpl positionInfoService) {
        this.positionInfoService = positionInfoService;
    }

    @PostMapping("/queryPositionLevel")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<CodeInfo>> queryPositionLevel() {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionInfoService.queryPositionLevel());
        } catch (Exception e) {
            log.error("职层query查询出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/saveOrUpdatePositionLevel")
    @Operation(summary = "保存或更新职层")
    @AuditLog(value = "保存或更新职层", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<Boolean> saveOrUpdatePositionLevel(CodeInfo codeInfo) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionInfoService.saveOrUpdatePositionLevel(codeInfo));
        } catch (Exception e) {
            log.error("职位query查询出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<PositionInfo>> pagePositionInfo(@Validated @RequestBody(required = false) PositionInfo pageReqVO,
                                                     @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                     @RequestParam(required = false, defaultValue = "10")int pageSize) {
        try {
            pageReqVO = (pageReqVO == null) ? new PositionInfo() : pageReqVO;
            Page<PositionInfo> page = new Page<>(pageNo, pageSize);
            positionInfoService.queryActivated(page, pageReqVO);
            positionInfoService.translateField(page.getRecords());
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
        } catch (Exception e) {
            log.error("职位page查询出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<PositionInfo>> queryPositionInfo(@Validated @RequestBody(required = false) PositionInfo queryReqVO) {
        try {
            queryReqVO = (queryReqVO == null) ? new PositionInfo() : queryReqVO;
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionInfoService.query(queryReqVO));
        } catch (Exception e) {
            log.error("职位query查询出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PositionInfo> getPositionInfo(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", positionInfoService.getById(id));
        }  catch (Exception e) {
            log.error("职位get查询出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/save")
    @Operation(summary = "创建")
    @AuditLog(value = "创建", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的对象", required = true)
    public ResponseResult<Boolean> createPositionInfo(@Valid @RequestBody PositionInfo createReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "创建成功", positionInfoService.save(createReqVO));
        }  catch (Exception e) {
            log.error("职位创建出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新")
    @AuditLog(value = "更新", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的职位", required = true)
    public ResponseResult<Boolean> updatePositionInfo(@Valid @RequestBody PositionInfo updateReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "更新成功", positionInfoService.saveOrUpdate(updateReqVO));
        }  catch (Exception e) {
            log.error("职位更新出现错误", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportPositionInfo(@Validated @RequestBody(required = false) PositionInfo exportReqVO,
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

        exportReqVO = (exportReqVO == null) ? new PositionInfo() : exportReqVO;
        List<PositionInfo> positionInfoList = positionInfoService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PositionInfo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(positionInfoList);
    }
}
