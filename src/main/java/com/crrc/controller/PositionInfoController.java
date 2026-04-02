package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.PositionInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.PositionInfoServiceImpl;
import com.crrc.utils.PositionInfoImportListener;
import com.crrc.utils.RoleUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @author yukun
 */
@RestController
@RequestMapping("/positionInfo")
@RefreshScope
@TenantIgnore
public class PositionInfoController {
    @Autowired
    private PositionInfoServiceImpl positionInfoService;

    @Autowired
    private JdbcTemplate openGaussJdbcTemplate;

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    @TenantIgnore
    public ResponseResult<IPage<PositionInfo>> pagePositionInfo(@Validated @RequestBody(required = false) PositionInfo pageReqVO,
                                                  @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                  @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PositionInfo() : pageReqVO;
        Page<PositionInfo> page = new Page<>(pageNo, pageSize);
        positionInfoService.queryByOrder(page, pageReqVO);
        positionInfoService.resolveField(page.getRecords());
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    @TenantIgnore
    public ResponseResult<List<PositionInfo>> queryPositionInfo(@Validated @RequestBody(required = false) PositionInfo queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PositionInfo() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionInfoService.query(queryReqVO));
    }

    @PostMapping("/queryList")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<PositionInfo>> queryPositionInfoByList(@Validated @RequestBody(required = false) List<String> codeList) {
        LambdaQueryWrapper<PositionInfo> wrapper = new LambdaQueryWrapper<>();
        if(codeList != null && !codeList.isEmpty()) {
            wrapper.in(PositionInfo::getCode, codeList);
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", positionInfoService.getBaseMapper().selectList(wrapper));
    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PositionInfo> getPositionInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", positionInfoService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createPositionInfo(@Valid @RequestBody PositionInfo createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", positionInfoService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updatePositionInfo(@Valid @RequestBody PositionInfo updateReqVO) {
        LambdaQueryWrapper<PositionInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PositionInfo::getCode, updateReqVO.getCode());
        if(positionInfoService.exists(wrapper)) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "职位编码不能重复");
        } else {
            return new ResponseResult<>(HttpStatus.OK.value(), "更新职位信息成功", positionInfoService.saveOrUpdate(updateReqVO));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositonInfo(@RequestBody List<Integer> idList) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", positionInfoService.removeBatchByIds(idList));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportPositionInfo(@Validated @RequestBody(required = false) PositionInfo exportReqVO,
                                   @RequestParam(required = false, defaultValue = "xlsx") String type,
                                   HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        exportReqVO = (exportReqVO == null) ? new PositionInfo() : exportReqVO;
        List<PositionInfo> positionInfoList = positionInfoService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), PositionInfo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(positionInfoList);
    }

//    @PostMapping("/setJobType")
//    @Transactional(rollbackFor = Exception.class)
//    public ResponseResult<Boolean> setJobType(@RequestBody MultipartFile file) throws IOException {
//        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
//            EasyExcel.read(file.getInputStream(), PositionInfo.class,
//                            new PositionInfoImportListener(positionInfoService))
//                    .headRowNumber(1)
//                    .sheet()
//                    .doRead();
//
//            return new ResponseResult<>(HttpStatus.OK.value(), true);
//        }
//        return new ResponseResult<>(HttpStatus.OK.value(), false);
//    }

    @PostMapping("/truncate/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        if (RoleUtils.checkAnyMatch("hrsc_admin")) {
            openGaussJdbcTemplate.execute("TRUNCATE TABLE position_info");

            EasyExcel.read(file.getInputStream(), PositionInfo.class,
                            new PositionInfoImportListener(positionInfoService))
                    .headRowNumber(1)
                    .sheet()
                    .doRead();

            return new ResponseResult<>(HttpStatus.OK.value(), true);
        }
        return new ResponseResult<>(HttpStatus.OK.value(), false);
    }
}


