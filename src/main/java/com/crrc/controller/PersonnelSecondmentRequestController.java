package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.PersonnelSecondmentRequestServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
 * 借调（借用）人员需求计划申请明细表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@RestController
@RequestMapping("/personnelSecondmentRequest")
@RefreshScope
@Tag(name = "借调（借用）人员需求计划申请明细表", description = "借调（借用）人员需求计划申请明细表")
public class PersonnelSecondmentRequestController {

    @Autowired
    private PersonnelSecondmentRequestServiceImpl personnelSecondmentRequestService;

    @PostMapping("/page")
    @Operation(summary = "获取借调（借用）人员需求计划申请明细表列表 page")
    @AuditLog(value = "获取借调（借用）人员需求计划申请明细表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<PersonnelSecondmentRequest>> pagePersonnelSecondmentRequest(@Validated @RequestBody(required = false) PersonnelSecondmentRequest pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PersonnelSecondmentRequest() : pageReqVO;
        Page<PersonnelSecondmentRequest> page = new Page<>(pageNo, pageSize);
        personnelSecondmentRequestService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请明细表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取借调（借用）人员需求计划申请明细表列表")
    @AuditLog(value = "获取借调（借用）人员需求计划申请明细表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<PersonnelSecondmentRequest>> queryPersonnelSecondmentRequest(@Validated @RequestBody(required = false) PersonnelSecondmentRequest queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PersonnelSecondmentRequest() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请明细表列表成功", personnelSecondmentRequestService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得借调（借用）人员需求计划申请明细表实体")
    @AuditLog(value = "获得借调（借用）人员需求计划申请明细表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PersonnelSecondmentRequest> getPersonnelSecondmentRequest(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请明细表实体成功", personnelSecondmentRequestService.getById(id));
    }

    @Transactional
    @PostMapping("/save/{relationId}")
    @Operation(summary = "保存借调（借用）人员需求计划申请明细表实体")
    @AuditLog(value = "借调（借用）人员需求计划申请明细表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> savePersonnelSecondmentRequest(@Valid @RequestBody List<PersonnelSecondmentRequest> reqVOList, @PathVariable String relationId) {

        reqVOList.forEach(
                item -> {
                    if(!StringUtils.hasText(item.getId())) {
                        item.setId(IdUtil.randomUUID());
                        item.setRelationId(relationId);
                    }
                }
        );

        boolean result = personnelSecondmentRequestService.saveDetails(relationId, reqVOList);

        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员需求计划申请明细表实体成功", result);
    }

    //    @Transactional
//    @PostMapping("/saveByUserCode")
//    @Operation(summary = "根据档案号保存借调（借用）人员需求计划申请明细表实体")
//    @AuditLog(value = "借调（借用）人员需求计划申请明细表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
//    @Parameter(name = "reqVO", description = "实体对象", required = true)
//    public ResponseResult<Boolean> saveByUserCode(@Valid @RequestBody PersonnelSecondmentRequest reqVO) {
//
//        LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(PersonnelSecondmentRequest::getUserCode, reqVO.getUserCode());
//        boolean result = personnelSecondmentRequestService.saveOrUpdate(reqVO, wrapper);
//
//        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员需求计划申请明细表实体成功", result);
//    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除借调（借用）人员需求计划申请明细表实体")
    @AuditLog(value = "删除借调（借用）人员需求计划申请明细表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePersonnelSecondmentRequest(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除借调（借用）人员需求计划申请明细表实体成功", personnelSecondmentRequestService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出借调（借用）人员需求计划申请明细表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出借调（借用）人员需求计划申请明细表报表")
    public void exportPersonnelSecondmentRequest(@Validated @RequestBody(required = false) PersonnelSecondmentRequest exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("借调（借用）人员需求计划申请明细表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new PersonnelSecondmentRequest() : exportReqVO;
        List<PersonnelSecondmentRequest> personnelSecondmentRequestList = personnelSecondmentRequestService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PersonnelSecondmentRequest.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("借调（借用）人员需求计划申请明细表报表")
                .doWrite(personnelSecondmentRequestList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入借调（借用）人员需求计划申请明细表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入借调（借用）人员需求计划申请明细表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PersonnelSecondmentRequest.class,
                new ImportExcelListener<PersonnelSecondmentRequest, PersonnelSecondmentRequestServiceImpl>(personnelSecondmentRequestService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入借调（借用）人员需求计划申请明细表报表成功");
    }

}
