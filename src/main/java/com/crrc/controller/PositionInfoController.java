package com.crrc.controller;

import com.alibaba.excel.EasyExcelFactory;
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
import com.crrc.pojo.dto.PositionInfoCopyRequest;
import com.crrc.pojo.vo.PositionInfoImportResultVO;
import com.crrc.service.impl.PositionInfoServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
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
        List<PositionInfo> list = positionInfoService.query(queryReqVO);
        positionInfoService.resolveField(list);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", list);
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
        List<PositionInfo> list = positionInfoService.getBaseMapper().selectList(wrapper);
        positionInfoService.resolveField(list);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", list);
    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PositionInfo> getPositionInfo(@RequestParam("id") Long id) {
        PositionInfo positionInfo = positionInfoService.getById(id);
        if (positionInfo != null) {
            positionInfoService.resolveField(Collections.singletonList(positionInfo));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", positionInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createPositionInfo(@Valid @RequestBody PositionInfo createReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", positionInfoService.saveOrUpdateWithValidation(createReqVO));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updatePositionInfo(@Valid @RequestBody PositionInfo updateReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "更新职位信息成功", positionInfoService.saveOrUpdateWithValidation(updateReqVO));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePositonInfo(@RequestBody List<Integer> idList) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", positionInfoService.removePositionInfoWithIncumbentCheck(idList));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    /**
     * @param file 导入文件，只输入编码code和标准岗级jobGrade
     * @return 导入结果
     */
    @PostMapping("/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        try {
            positionInfoService.importJobGrade(file);
            return new ResponseResult<>(HttpStatus.OK.value(), "批量导入标准岗级成功！");
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "批量导入失败，" + e.getMessage());
        }
    }

    /**
     * 完整职位信息导入，支持新增和按职位编码更新。
     */
    @PostMapping("/importFull")
    @AuditLog(value = "完整导入职位信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "完整导入职位信息")
    public ResponseResult<PositionInfoImportResultVO> importFull(MultipartFile file) throws IOException {
        try {
            PositionInfoImportResultVO result = positionInfoService.importFull(file);
            return new ResponseResult<>(HttpStatus.OK.value(), "完整导入职位信息成功", result);
        } catch (Exception e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "完整导入职位信息失败，" + e.getMessage());
        }
    }

    /**
     * 复制职位到指定组织单元。
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/copyToOrgUnit")
    @AuditLog(value = "复制职位到组织单元", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Operation(summary = "复制职位到组织单元")
    public ResponseResult<List<PositionInfo>> copyToOrgUnit(@RequestBody PositionInfoCopyRequest request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "复制职位成功", positionInfoService.copyToOrgUnit(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    /**
     * 刷新在岗人数/在岗人员快照。请求体为空时刷新全部职位。
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/refreshIncumbents")
    @AuditLog(value = "刷新职位在岗人员", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Operation(summary = "刷新职位在岗人员")
    public ResponseResult<Integer> refreshIncumbents(@RequestBody(required = false) List<String> codeList) {
        return new ResponseResult<>(HttpStatus.OK.value(), "刷新职位在岗人员成功", positionInfoService.refreshIncumbents(codeList));
    }

    /**
     * 获得编码code和标准岗级jobGrade导入模板。
     */
    @PostMapping("/export")
    @AuditLog(value = "导出标准岗级导入模板", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出标准岗级导入模板")
    public void imports(@RequestParam(required = false, defaultValue = "xlsx") String type,
                        HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("职位标准岗级导入模板-" + new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        List<List<String>> exampleList = Collections.singletonList(Arrays.asList("132010049", "40"));
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcelFactory.write(response.getOutputStream())
                .head(Arrays.asList(
                        Collections.singletonList("编码"),
                        Collections.singletonList("标准岗级"),
                        Collections.singletonList("填写职位编码和对应的标准岗级，不要导入不存在的职位编码，解密文件后导入")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("职位标准岗级导入模板")
                .doWrite(exampleList);
    }

    /**
     * 完整职位信息导入模板。
     */
    @PostMapping("/exportFullTemplate")
    @AuditLog(value = "导出完整职位信息导入模板", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出完整职位信息导入模板")
    public void exportFullTemplate(@RequestParam(required = false, defaultValue = "xlsx") String type,
                                   HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("职位信息完整导入模板-" + new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        List<List<String>> head = Arrays.asList(
                Collections.singletonList("职位编码"),
                Collections.singletonList("职位名称"),
                Collections.singletonList("版本"),
                Collections.singletonList("生效日期"),
                Collections.singletonList("撤销日期"),
                Collections.singletonList("所属组织单元"),
                Collections.singletonList("工作中心"),
                Collections.singletonList("职类"),
                Collections.singletonList("职群"),
                Collections.singletonList("职种"),
                Collections.singletonList("职层"),
                Collections.singletonList("标准职级"),
                Collections.singletonList("编制人数"),
                Collections.singletonList("控编模式"),
                Collections.singletonList("部门代码"),
                Collections.singletonList("部门名称"),
                Collections.singletonList("上级职位编码"),
                Collections.singletonList("上级职位名称"),
                Collections.singletonList("关联职务"),
                Collections.singletonList("工作地点"),
                Collections.singletonList("工作内容"),
                Collections.singletonList("职位状态")
        );
        List<List<String>> exampleList = Collections.singletonList(Arrays.asList(
                "013200001", "信息化工程师", "1.0", "2026-01-01", "", "10000001", "", "A", "A-01", "A-01-001", "P3", "40", "1", "编制控制", "10000001", "信息化部", "", "", "", "长春", "负责信息化系统建设与运维", "启用"
        ));
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcelFactory.write(response.getOutputStream())
                .head(head)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("职位信息完整导入模板")
                .doWrite(exampleList);
    }
}
