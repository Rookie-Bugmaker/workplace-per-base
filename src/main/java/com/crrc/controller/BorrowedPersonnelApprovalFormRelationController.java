package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantScope;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.BorrowedPersonnelApprovalForm;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import com.crrc.pojo.dto.BorrowedPersonnelApprovalFormRelationDetailDTO;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.BorrowedPersonnelApprovalFormRelationServiceImpl;
import com.crrc.service.impl.BorrowedPersonnelApprovalFormServiceImpl;
import com.crrc.service.impl.PersonnelSecondmentRequestRelationServiceImpl;
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
 * 借调（借用）人员审批表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@RestController
@RequestMapping("/borrowedPersonnelApprovalFormRelation")
@RefreshScope
@Tag(name = "借调（借用）人员审批表", description = "借调（借用）人员审批表")
@TenantScope(filterRoleCode = {"DISPATCH_ADMIN", "QUOTA_ADMIN", "hrsc_deputy_minister"}, tenantLevel = TenantScope.TenantLevel.DEPT)
public class BorrowedPersonnelApprovalFormRelationController {

    @Autowired
    private BorrowedPersonnelApprovalFormRelationServiceImpl borrowedPersonnelApprovalFormRelationService;
    @Autowired
    private PersonnelSecondmentRequestRelationServiceImpl personnelSecondmentRequestRelationService;
    @Autowired
    private BorrowedPersonnelApprovalFormServiceImpl borrowedPersonnelApprovalFormService;

    @PostMapping("/page")
    @Operation(summary = "获取借调（借用）人员审批表列表 page")
    @AuditLog(value = "获取借调（借用）人员审批表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage<PersonnelSecondmentRequestRelation>> pageBorrowedPersonnelApprovalFormRelation(
            @Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation pageReqVO,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "10")int pageSize
    ) {
        pageReqVO = (pageReqVO == null) ? new PersonnelSecondmentRequestRelation() : pageReqVO;
        Page<PersonnelSecondmentRequestRelation> page = new Page<>(pageNo, pageSize);
        personnelSecondmentRequestRelationService.pageBySourceDept(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取借调（借用）人员审批表列表")
    @AuditLog(value = "获取借调（借用）人员审批表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<BorrowedPersonnelApprovalFormRelation>> queryBorrowedPersonnelApprovalFormRelation(@Validated @RequestBody(required = false) BorrowedPersonnelApprovalFormRelation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new BorrowedPersonnelApprovalFormRelation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表列表成功", borrowedPersonnelApprovalFormRelationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得借调（借用）人员审批表实体")
    @AuditLog(value = "获得借调（借用）人员审批表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<BorrowedPersonnelApprovalFormRelation> getBorrowedPersonnelApprovalFormRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表实体成功", borrowedPersonnelApprovalFormRelationService.getById(id));
    }

    @Transactional
    @PostMapping("/saveDetails/{requestId}")
    @Operation(summary = "保存借调（借用）人员审批表实体")
    @AuditLog(value = "借调（借用）人员审批表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> saveBorrowedPersonnelApprovalFormRelation(@PathVariable String requestId, @Valid @RequestBody BorrowedPersonnelApprovalFormRelationDetailDTO reqDTO) {

        BorrowedPersonnelApprovalFormRelation relation = reqDTO.getRelation();

        PersonnelSecondmentRequestRelation request = personnelSecondmentRequestRelationService.getById(requestId);

        if(!StringUtils.hasText(relation.getId())) {
            String id = IdUtil.randomUUID();
            relation.setId(id);
            request.setPersonnelRelationId(id);
            request.setOrderStatus(AuditConstants.STATUS_AUDIT);
        }

        personnelSecondmentRequestRelationService.updateById(request);
        borrowedPersonnelApprovalFormRelationService.saveOrUpdate(relation);

        List<BorrowedPersonnelApprovalForm> reqVOList = reqDTO.getApprovalFormList();

        reqVOList.forEach(
            item -> {
                if(!StringUtils.hasText(item.getId())) {
                    item.setId(IdUtil.randomUUID());
                    item.setRelationId(relation.getId());
                }
            }
        );

        boolean result = borrowedPersonnelApprovalFormService.saveDetails(relation.getId(), reqVOList);
    
        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员审批表实体成功", result);
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除借调（借用）人员审批表实体")
    @AuditLog(value = "删除借调（借用）人员审批表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteBorrowedPersonnelApprovalFormRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除借调（借用）人员审批表实体成功", borrowedPersonnelApprovalFormRelationService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出借调（借用）人员审批表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出借调（借用）人员审批表报表")
    public void exportBorrowedPersonnelApprovalFormRelation(@Validated @RequestBody(required = false) BorrowedPersonnelApprovalFormRelation exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("借调（借用）人员审批表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new BorrowedPersonnelApprovalFormRelation() : exportReqVO;
        List<BorrowedPersonnelApprovalFormRelation> borrowedPersonnelApprovalFormRelationList = borrowedPersonnelApprovalFormRelationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), BorrowedPersonnelApprovalFormRelation.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("借调（借用）人员审批表报表")
                .doWrite(borrowedPersonnelApprovalFormRelationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入借调（借用）人员审批表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入借调（借用）人员审批表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), BorrowedPersonnelApprovalFormRelation.class,
                new ImportExcelListener<BorrowedPersonnelApprovalFormRelation, BorrowedPersonnelApprovalFormRelationServiceImpl>(borrowedPersonnelApprovalFormRelationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入借调（借用）人员审批表报表成功");
    }

}