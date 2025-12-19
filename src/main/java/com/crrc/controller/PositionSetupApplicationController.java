package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.entity.PositionSetupApplication;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowUserPageDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.PositionSetupApplicationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
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
 * @author yukun
 * @since 2025-12-19 11:03:43
 */
@RestController
@RequestMapping("/positionSetupApplication")
@RefreshScope
@Slf4j
public class PositionSetupApplicationController {

    private final PositionSetupApplicationServiceImpl positionSetupApplicationService;

    @Autowired
    public PositionSetupApplicationController(PositionSetupApplicationServiceImpl positionSetupApplicationService) {
        this.positionSetupApplicationService = positionSetupApplicationService;
    }

    /**
     * 查询职位申请审批人用户列表
     * @param page     当前页码，page===null则查询全部
     * @param pageSize 查询数据条数，pageSize==null则查询全部
     * @return 用户列表
     */
    @GetMapping("/findApproval")
    public ResponseResult<WorkflowUserPageDTO> findUserByAgreementCode(Integer page, Integer pageSize) {
        try {
            WorkflowUserPageDTO workflowUserPage = positionSetupApplicationService.findApproval(page, pageSize);
            return ResponseResult.success(workflowUserPage);
        } catch (Exception e) {
            log.error("查询审批人错误", e);
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR");
        }
    }

    /**
     * 工作流回写
     *
     * @param param 参数
     */
    @PutMapping("/write-back")
    @IgnoreTenant
    public ResponseResult<Boolean> writeBack(@RequestBody WorkflowWriteBackParamDTO param) {
        try {
            positionSetupApplicationService.processWriteBack(param);
            return new ResponseResult<>(HttpStatus.OK.value(), "回写成功", true);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<PositionSetupApplication>> pagePositionSetupApplication(@Validated @RequestBody(required = false) PositionSetupApplication pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PositionSetupApplication() : pageReqVO;
        Page<PositionSetupApplication> page = new Page<>(pageNo, pageSize);
        positionSetupApplicationService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<PositionSetupApplication>> queryPositionSetupApplication(@Validated @RequestBody(required = false) PositionSetupApplication queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PositionSetupApplication() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionSetupApplicationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PositionSetupApplication> getPositionSetupApplication(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", positionSetupApplicationService.getById(id));
    }

    @Transactional
    @PostMapping("/updateDraft")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updatePositionSetupApplication(@Valid @RequestBody PositionSetupApplication updateReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "保存或更新草稿成功", positionSetupApplicationService.saveOrUpdateDraft(updateReqVO));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存或更新草稿失败");
        }

    }

    @Transactional
    @GetMapping("/deleteDraft")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositionSetupApplication(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "删除草稿成功", positionSetupApplicationService.removeDraftById(id));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "删除草稿失败");
        }
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportPositionSetupApplication(@Validated @RequestBody(required = false) PositionSetupApplication exportReqVO,
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

        exportReqVO = (exportReqVO == null) ? new PositionSetupApplication() : exportReqVO;
        List<PositionSetupApplication> positionSetupApplicationList = positionSetupApplicationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PositionSetupApplication.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(positionSetupApplicationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PositionSetupApplication.class,
                new ImportExcelListener<PositionSetupApplication, PositionSetupApplicationServiceImpl>(positionSetupApplicationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入报表成功");
    }

}
