package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.PositionDescription;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.PositionDescriptionAuditDTO;
import com.crrc.pojo.dto.PositionDescriptionSaveDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionDescriptionExportBundleVO;
import com.crrc.pojo.vo.PositionDescriptionMainExportVO;
import com.crrc.pojo.vo.PositionDescriptionTodoVO;
import com.crrc.pojo.vo.PositionDescriptionWorkflowRoleVO;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.service.PositionDescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 职位说明书管理前端控制器。
 */
@RestController
@RequestMapping("/positionDescription")
@RefreshScope
@TenantIgnore
@Tag(name = "职位说明书管理", description = "职位说明书管理")
public class PositionDescriptionController {

    @Autowired
    private PositionDescriptionService positionDescriptionService;

    @PostMapping("/page")
    @Operation(summary = "职位说明书分页查询")
    @AuditLog(value = "职位说明书分页查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的职位说明书对象", required = false)
    public ResponseResult<IPage<PositionDescription>> page(@Validated @RequestBody(required = false) PositionDescription pageReqVO,
                                                           @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                           @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Page<PositionDescription> page = new Page<>(pageNo, pageSize);
        positionDescriptionService.queryPage(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书分页查询成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "职位说明书列表查询")
    @AuditLog(value = "职位说明书列表查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionDescription>> query(@Validated @RequestBody(required = false) PositionDescription queryReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书列表查询成功", positionDescriptionService.queryList(queryReqVO));
    }


    @PostMapping("/export")
    @Operation(summary = "导出职位说明书列表")
    @AuditLog(value = "导出职位说明书列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    public void export(@Validated @RequestBody(required = false) PositionDescription exportReqVO,
                       @RequestParam(required = false, defaultValue = "xlsx") String type,
                       HttpServletResponse response) throws IOException {
        prepareExportResponse(response, "职位说明书列表-", type);
        List<PositionDescriptionMainExportVO> exportList = positionDescriptionService.exportMainList(exportReqVO);
        EasyExcel.write(response.getOutputStream(), PositionDescriptionMainExportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(resolveExcelType(type))
                .sheet("职位说明书列表")
                .doWrite(exportList);
    }

    @GetMapping("/get")
    @Operation(summary = "职位说明书详情查询")
    @AuditLog(value = "职位说明书详情查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<PositionDescriptionDetailVO> get(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书详情查询成功", positionDescriptionService.getDetail(id));
    }

    @GetMapping("/getLatestByPositionCode")
    @Operation(summary = "按职位编码查询当前说明书")
    @AuditLog(value = "按职位编码查询当前说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<PositionDescriptionDetailVO> getLatestByPositionCode(@RequestParam("positionCode") String positionCode) {
        return new ResponseResult<>(HttpStatus.OK.value(), "按职位编码查询当前说明书成功", positionDescriptionService.getLatestByPositionCode(positionCode));
    }

    @PostMapping("/saveDraft")
    @Operation(summary = "保存职位说明书草稿")
    @AuditLog(value = "保存职位说明书草稿", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<PositionDescriptionDetailVO> saveDraft(@Valid @RequestBody PositionDescriptionSaveDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书草稿保存成功", positionDescriptionService.saveDraft(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/submit")
    @Operation(summary = "提交职位说明书审批")
    @AuditLog(value = "提交职位说明书审批", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<PositionDescription> submit(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书提交审批成功", positionDescriptionService.submit(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/approve")
    @Operation(summary = "职位说明书审批通过")
    @AuditLog(value = "职位说明书审批通过", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> approve(@RequestBody PositionDescriptionAuditDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批通过成功", positionDescriptionService.approve(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/reject")
    @Operation(summary = "职位说明书审批驳回")
    @AuditLog(value = "职位说明书审批驳回", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> reject(@RequestBody PositionDescriptionAuditDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批驳回成功", positionDescriptionService.reject(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PutMapping("/approval-callback")
    @Operation(summary = "职位说明书Flowable回调")
    @AuditLog(value = "职位说明书Flowable回调", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> approvalCallback(@RequestBody WorkflowWriteBackParamDTO param) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书Flowable回调处理成功", positionDescriptionService.workflowCallback(param));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/publish")
    @Operation(summary = "发布职位说明书")
    @AuditLog(value = "发布职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> publish(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书发布成功", positionDescriptionService.publish(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/disable")
    @Operation(summary = "停用职位说明书")
    @AuditLog(value = "停用职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> disable(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书停用成功", positionDescriptionService.disable(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/enable")
    @Operation(summary = "启用职位说明书")
    @AuditLog(value = "启用职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> enable(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书启用成功", positionDescriptionService.enable(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "删除已撤销职位说明书")
    @AuditLog(value = "删除已撤销职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    public ResponseResult<Boolean> delete(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书删除成功", positionDescriptionService.deleteCanceled(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/processTrace")
    @Operation(summary = "职位说明书审批流程跟踪")
    @AuditLog(value = "职位说明书审批流程跟踪", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<ProcessRunningInfoVo> processTrace(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批流程跟踪查询成功", positionDescriptionService.runningSequenceGraph(id));
    }

    @GetMapping("/workflowRoles")
    @Operation(summary = "职位说明书审批角色配置查询")
    @AuditLog(value = "职位说明书审批角色配置查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionDescriptionWorkflowRoleVO>> workflowRoles() {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批角色配置查询成功", positionDescriptionService.workflowRoles());
    }

    @GetMapping("/currentUserTodo")
    @Operation(summary = "当前用户职位说明书审批待办")
    @AuditLog(value = "当前用户职位说明书审批待办", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionDescriptionTodoVO>> currentUserTodo() {
        return new ResponseResult<>(HttpStatus.OK.value(), "当前用户职位说明书审批待办查询成功", positionDescriptionService.currentUserTodo());
    }

    @PostMapping("/myPage")
    @Operation(summary = "我的职位说明书申请分页")
    @AuditLog(value = "我的职位说明书申请分页", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<IPage<PositionDescription>> myPage(@Validated @RequestBody(required = false) PositionDescription pageReqVO,
                                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                             @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Page<PositionDescription> page = new Page<>(pageNo, pageSize);
        positionDescriptionService.myCreatedPage(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "我的职位说明书申请分页查询成功", page);
    }

    @PostMapping("/statusStats")
    @Operation(summary = "职位说明书状态统计")
    @AuditLog(value = "职位说明书状态统计", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<Map<String, Long>> statusStats(@Validated @RequestBody(required = false) PositionDescription queryReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书状态统计成功", positionDescriptionService.statusStatistics(queryReqVO));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消/撤销职位说明书")
    @AuditLog(value = "取消/撤销职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> cancel(@RequestParam("id") Long id,
                                          @RequestParam(required = false) String reason) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书取消成功", positionDescriptionService.cancel(id, reason));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }


    @GetMapping("/exportDetail")
    @Operation(summary = "导出职位说明书明细")
    @AuditLog(value = "导出职位说明书明细", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    public void exportDetail(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {
        PositionDescriptionExportBundleVO bundle = positionDescriptionService.buildExportBundle(id);
        prepareXlsxResponse(response, "职位说明书明细-");
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .autoCloseStream(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .build();
            writeSheet(excelWriter, 0, "基本信息", com.crrc.pojo.vo.PositionDescriptionMainExportVO.class, bundle.getMainRows());
            writeSheet(excelWriter, 1, "职位职责", com.crrc.pojo.vo.PositionDescriptionResponsibilityExportVO.class, bundle.getResponsibilityRows());
            writeSheet(excelWriter, 2, "工作依据", com.crrc.pojo.vo.PositionDescriptionBasisExportVO.class, bundle.getBasisRows());
            writeSheet(excelWriter, 3, "协调内容", com.crrc.pojo.vo.PositionDescriptionCoordinationExportVO.class, bundle.getCoordinationRows());
            writeSheet(excelWriter, 4, "工作权限", com.crrc.pojo.vo.PositionDescriptionAuthorityExportVO.class, bundle.getAuthorityRows());
            writeSheet(excelWriter, 5, "考核方向", com.crrc.pojo.vo.PositionDescriptionAssessmentExportVO.class, bundle.getAssessmentRows());
            writeSheet(excelWriter, 6, "能力要求", com.crrc.pojo.vo.PositionDescriptionCompetencyExportVO.class, bundle.getCompetencyRows());
            writeSheet(excelWriter, 7, "附件", com.crrc.pojo.vo.PositionDescriptionAttachmentExportVO.class, bundle.getAttachmentRows());
            writeSheet(excelWriter, 8, "审批记录", com.crrc.pojo.vo.PositionDescriptionAuditRecordExportVO.class, bundle.getAuditRecordRows());
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }



    private void prepareExportResponse(HttpServletResponse response, String fileNamePrefix, String type) throws IOException {
        String safeType = normalizeExportType(type);
        String contentType = "csv".equals(safeType) ? "text/csv;charset=UTF-8" :
                ("xls".equals(safeType) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(fileNamePrefix + new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + safeType);
    }

    private ExcelTypeEnum resolveExcelType(String type) {
        String safeType = normalizeExportType(type);
        return "csv".equals(safeType) ? ExcelTypeEnum.CSV : ("xls".equals(safeType) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
    }

    private String normalizeExportType(String type) {
        if ("csv".equalsIgnoreCase(type)) {
            return "csv";
        }
        if ("xls".equalsIgnoreCase(type)) {
            return "xls";
        }
        return "xlsx";
    }

    private void prepareXlsxResponse(HttpServletResponse response, String fileNamePrefix) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(fileNamePrefix + new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void writeSheet(ExcelWriter excelWriter, int sheetNo, String sheetName, Class headClass, List data) {
        WriteSheet sheet = EasyExcel.writerSheet(sheetNo, sheetName).head(headClass).build();
        excelWriter.write(data, sheet);
    }
}
