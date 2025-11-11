package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.QueryMethod;
import com.crrc.entity.CostCenter;
import com.crrc.entity.HrEmpBasic;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.enums.QueryType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.BundleEmpAndCostCenterVO;
import com.crrc.pojo.vo.CostCenterImportVO;
import com.crrc.pojo.vo.HrBasicCostCenterDetailVO;
import com.crrc.service.impl.CostCenterServiceImpl;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.utils.CostCenterCustomImportListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author yukun
 */
@RestController
@RequestMapping("/costCenter")
@RefreshScope
public class CostCenterController {
    @Autowired
    private CostCenterServiceImpl costCenterService;
    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage<CostCenter>> pageCostCenter(@Validated @RequestBody(required = false) CostCenter pageReqVO,
                                                  @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                  @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new CostCenter() : pageReqVO;
        Page<CostCenter> page = new Page<>(pageNo, pageSize);
        costCenterService.queryVO(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<CostCenter>> queryCostCenter(@Validated @RequestBody(required = false) CostCenter queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new CostCenter() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", costCenterService.queryVO(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<CostCenter> getCostCenter(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", costCenterService.getVO(id));
    }

    @Transactional
    @PostMapping("/bundleEmp")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> bundleEmpAndCostCenter(@RequestBody List<BundleEmpAndCostCenterVO> vo) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", costCenterService.bundleEmpAndCostCenter(vo));
    }

    @Transactional
    @GetMapping("/queryDetails")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<List<HrBasicCostCenterDetailVO>> queryDetails(@RequestParam Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", costCenterService.queryDetails(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createCostCenter(@Valid @RequestBody CostCenter createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", costCenterService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateCostCenter(@Valid @RequestBody CostCenter updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新实体成功", costCenterService.saveOrUpdate(updateReqVO));
    }

    @PostMapping("/deleteUserFromCostCenter")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositonInfo(@RequestBody BundleEmpAndCostCenterVO vo) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", costCenterService.removeUserFromCostCenter(vo));
    }

    @GetMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositonInfo(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", costCenterService.removeByIdAndCheck(id));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportCostCenter(@Validated @RequestBody(required = false) CostCenter exportReqVO,
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

        exportReqVO = (exportReqVO == null) ? new CostCenter() : exportReqVO;
        List<CostCenter> positionInfoList = costCenterService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), CostCenter.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(positionInfoList);
    }

    @PostMapping("/download/template")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportCostCenter(@RequestParam(required = false, defaultValue = "xlsx") String type,
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

        List<CostCenterImportVO> positionInfoList = Collections.singletonList(new CostCenterImportVO("013200012345"));
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), CostCenterImportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(positionInfoList);
    }

    @PostMapping("/import/{costCenterId}")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file, @PathVariable Long costCenterId) throws IOException {
        try {
            EasyExcelFactory.read(file.getInputStream(), CostCenterImportVO.class,
                            new CostCenterCustomImportListener(hrEmpBasicService, costCenterId))
                    .headRowNumber(1)
                    .sheet()
                    .doRead();
            return new ResponseResult<>(HttpStatus.OK.value(), "导入报表成功");
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_GATEWAY.value(), e.getMessage());
        }

    }
}
