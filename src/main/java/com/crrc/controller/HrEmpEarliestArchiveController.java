package com.crrc.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpEarliestArchive;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpEarliestArchiveServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 员工早期档案数据 前端控制器
 *
 * <p>该控制器提供只读的查询接口，不支持增删改操作。
 * <ul>
 *     <li>POST /hrEmpEarliestArchive/page - 分页查询</li>
 *     <li>POST /hrEmpEarliestArchive/query - 列表查询（不分页）</li>
 *     <li>GET /hrEmpEarliestArchive/get - 根据档案号精确查询</li>
 *     <li>POST /hrEmpEarliestArchive/export - 导出 Excel/CSV</li>
 * </ul>
 * </p>
 *
 * @author yukun
 * @since 2026-06-24
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpEarliestArchive")
@RefreshScope
@Tag(name = "员工早期档案数据", description = "员工早期档案数据查询接口")
public class HrEmpEarliestArchiveController {

    @Autowired
    private HrEmpEarliestArchiveServiceImpl hrEmpEarliestArchiveService;

    @PostMapping("/page")
    @Operation(summary = "分页查询员工早期档案数据")
    @AuditLog(value = "分页查询员工早期档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "queryEntity", description = "查询条件实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页大小", example = "10")
    public ResponseResult<IPage<HrEmpEarliestArchive>> pageHrEmpEarliestArchive(
            @Validated @RequestBody(required = false) HrEmpEarliestArchive queryEntity,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        queryEntity = (queryEntity == null) ? new HrEmpEarliestArchive() : queryEntity;

        try {
            Page<HrEmpEarliestArchive> page = new Page<>(pageNo, pageSize);
            IPage<HrEmpEarliestArchive> result = hrEmpEarliestArchiveService.pageByCondition(page, queryEntity);
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            log.error("分页查询员工早期档案数据异常", e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "查询员工早期档案数据（不分页）")
    @AuditLog(value = "查询员工早期档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "queryEntity", description = "查询条件实体对象", required = false)
    public ResponseResult<List<HrEmpEarliestArchive>> queryHrEmpEarliestArchive(
            @Validated @RequestBody(required = false) HrEmpEarliestArchive queryEntity) {

        queryEntity = (queryEntity == null) ? new HrEmpEarliestArchive() : queryEntity;

        try {
            List<HrEmpEarliestArchive> result = hrEmpEarliestArchiveService.queryByCondition(queryEntity);
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            log.error("查询员工早期档案数据异常", e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @GetMapping("/get")
    @Operation(summary = "根据档案号查询员工早期档案")
    @AuditLog(value = "根据档案号查询员工早期档案", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "da1", description = "员工档案号（DA1）", required = true, example = "28035")
    public ResponseResult<HrEmpEarliestArchive> getHrEmpEarliestArchive(@RequestParam("da1") String da1) {
        try {
            HrEmpEarliestArchive result = hrEmpEarliestArchiveService.getByDa1(da1);

            if (result == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "未找到该档案号的员工信息");
            }

            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            log.error("根据档案号查询员工早期档案异常", e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportHrEmpEarliestArchive(@Validated @RequestBody(required = false) HrEmpEarliestArchive exportReqVO,
                                 @RequestParam(required = false, defaultValue = "xlsx") String type,
                                 HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpEarliestArchive() : exportReqVO;
        List<HrEmpEarliestArchive> archiveList = hrEmpEarliestArchiveService.queryByCondition(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcelFactory.write(response.getOutputStream(), HrEmpEarliestArchive.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(archiveList);
    }
}
