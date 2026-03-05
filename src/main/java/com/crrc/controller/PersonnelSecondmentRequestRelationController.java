package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.pojo.dto.PersonnelSecondmentRequestRelationDetailDTO;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.PersonnelSecondmentRequestRelationTemplateVO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestTemplateVO;
import com.crrc.service.impl.FileServiceImpl;
import com.crrc.service.impl.PersonnelSecondmentRequestRelationServiceImpl;
import com.crrc.service.impl.PersonnelSecondmentRequestServiceImpl;
import com.crrc.utils.WordTemplateProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@RestController
@RequestMapping("/personnelSecondmentRequestRelation")
@RefreshScope
@Tag(name = "借调（借用）人员需求计划申请表表头", description = "借调（借用）人员需求计划申请表表头")
public class PersonnelSecondmentRequestRelationController {

    @Autowired
    private PersonnelSecondmentRequestRelationServiceImpl personnelSecondmentRequestRelationService;
    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private PersonnelSecondmentRequestServiceImpl personnelSecondmentRequestService;

    @PostMapping("/page")
    @Operation(summary = "获取借调（借用）人员需求计划申请表表头列表 page")
    @AuditLog(value = "获取借调（借用）人员需求计划申请表表头列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage<PersonnelSecondmentRequestRelation>> pagePersonnelSecondmentRequestRelation(@Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PersonnelSecondmentRequestRelation() : pageReqVO;
        Page<PersonnelSecondmentRequestRelation> page = new Page<>(pageNo, pageSize);
        personnelSecondmentRequestRelationService.query(page, pageReqVO);
        personnelSecondmentRequestRelationService.resolveFields(page.getRecords());
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表表头列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取借调（借用）人员需求计划申请表表头列表")
    @AuditLog(value = "获取借调（借用）人员需求计划申请表表头列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<PersonnelSecondmentRequestRelation>> queryPersonnelSecondmentRequestRelation(@Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PersonnelSecondmentRequestRelation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表表头列表成功", personnelSecondmentRequestRelationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得借调（借用）人员需求计划申请表表头实体")
    @AuditLog(value = "获得借调（借用）人员需求计划申请表表头实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PersonnelSecondmentRequestRelationDetailDTO> getPersonnelSecondmentRequestRelation(@RequestParam("id") String id) {
        PersonnelSecondmentRequestRelation relation = personnelSecondmentRequestRelationService.getById(id);
        personnelSecondmentRequestRelationService.resolveFields(Collections.singletonList(relation));
        List<PersonnelSecondmentRequest> requestList = personnelSecondmentRequestService.getByRelationId(id);
        PersonnelSecondmentRequestRelationDetailDTO detailDTO = new PersonnelSecondmentRequestRelationDetailDTO();
        detailDTO.setRelation(relation);
        detailDTO.setRequestList(requestList);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表表头实体成功", detailDTO);
    }

    @Transactional
    @PostMapping("/save")
    @Operation(summary = "保存借调（借用）人员需求计划申请表表头实体")
    @AuditLog(value = "借调（借用）人员需求计划申请表表头实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> savePersonnelSecondmentRequestRelation(@Valid @RequestBody PersonnelSecondmentRequestRelation reqVO) {

        if(!StringUtils.hasText(reqVO.getId())) {
            reqVO.setId(IdUtil.randomUUID());
        }

        boolean result = personnelSecondmentRequestRelationService.saveOrUpdate(reqVO);
    
        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员需求计划申请表表头实体成功", result);
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除借调（借用）人员需求计划申请表表头实体")
    @AuditLog(value = "删除借调（借用）人员需求计划申请表表头实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePersonnelSecondmentRequestRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除借调（借用）人员需求计划申请表表头实体成功", personnelSecondmentRequestRelationService.removeById(id));
    }

    @PostMapping("/generateRequestWord")
    public ResponseResult<String> generateRequestWordTest(@RequestParam String id) throws Exception {
        PersonnelSecondmentRequestRelation relation = personnelSecondmentRequestRelationService.getById(id);

        // 翻译部门
        personnelSecondmentRequestRelationService.resolveFields(Collections.singletonList(relation));

        PersonnelSecondmentRequestRelationTemplateVO relationVO = new PersonnelSecondmentRequestRelationTemplateVO();
        BeanUtils.copyProperties(relation, relationVO);
        // 需要单独把relation的翻译结果copy到relationVO中
        relationVO.setRequestingDepartment(relation.getRequestingDepartmentValue());

        LocalDate date = LocalDate.now();

        relationVO.setDay(String.valueOf(date.getDayOfMonth()));
        relationVO.setMonth(String.valueOf(date.getMonth().getValue()));
        relationVO.setYear(String.valueOf(date.getYear()));

        LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
        List<PersonnelSecondmentRequest> requestList = personnelSecondmentRequestService.getBaseMapper().selectList(wrapper);
        final int[] index = {1};
        List<PersonnelSecondmentRequestTemplateVO> requestVOList = requestList.stream().map(request -> {
            PersonnelSecondmentRequestTemplateVO vo = new PersonnelSecondmentRequestTemplateVO();
            BeanUtils.copyProperties(request, vo);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            vo.setStartEndDate(String.format("%s至%s", request.getStartDate().format(dateTimeFormatter), request.getEndDate().format(dateTimeFormatter)));
            vo.setIndex(index[0]);
            index[0] = index[0] + 1;
            return vo;
        }).collect(Collectors.toList());

        FileDataStream fileStream = fileService.getFileStream("abed7f9f-382c-4daf-a83b-adcf457c7fe5");
        String fileId;
        try {
            MultipartFile docx = WordTemplateProcessor.processTemplate(fileStream.getStream(), relationVO, requestVOList);
            fileId = fileService.upload(docx);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", fileId);
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出借调（借用）人员需求计划申请表表头报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出借调（借用）人员需求计划申请表表头报表")
    public void exportPersonnelSecondmentRequestRelation(@Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("借调（借用）人员需求计划申请表表头报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new PersonnelSecondmentRequestRelation() : exportReqVO;
        List<PersonnelSecondmentRequestRelation> personnelSecondmentRequestRelationList = personnelSecondmentRequestRelationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PersonnelSecondmentRequestRelation.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("借调（借用）人员需求计划申请表表头报表")
                .doWrite(personnelSecondmentRequestRelationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入借调（借用）人员需求计划申请表表头报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入借调（借用）人员需求计划申请表表头报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PersonnelSecondmentRequestRelation.class,
                new ImportExcelListener<PersonnelSecondmentRequestRelation, PersonnelSecondmentRequestRelationServiceImpl>(personnelSecondmentRequestRelationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入借调（借用）人员需求计划申请表表头报表成功");
    }

}
