package com.crrc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.ExceptionCasePerson;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.ExceptionCasePersonServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

/**
 * <p>
 * 额外人员信息表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@RestController
@RequestMapping("/exceptionCasePerson")
@RefreshScope
@Tag(name = "额外人员信息表", description = "额外人员信息表")
public class ExceptionCasePersonController {

    @Autowired
    private ExceptionCasePersonServiceImpl exceptionCasePersonService;

    @PostMapping("/page")
    @Operation(summary = "获取额外人员信息表列表 page")
    @AuditLog(value = "获取额外人员信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<ExceptionCasePerson>> pageExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new ExceptionCasePerson() : pageReqVO;
        Page<ExceptionCasePerson> page = new Page<>(pageNo, pageSize);
        exceptionCasePersonService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取额外人员信息表列表")
    @AuditLog(value = "获取额外人员信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<ExceptionCasePerson>> queryExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new ExceptionCasePerson() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表列表成功", exceptionCasePersonService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得额外人员信息表实体")
    @AuditLog(value = "获得额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<ExceptionCasePerson> getExceptionCasePerson(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表实体成功", exceptionCasePersonService.getById(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建额外人员信息表实体")
    @AuditLog(value = "创建额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createExceptionCasePerson(@Valid @RequestBody ExceptionCasePerson createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建额外人员信息表实体成功", exceptionCasePersonService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新额外人员信息表实体")
    @AuditLog(value = "更新额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateExceptionCasePerson(@Valid @RequestBody ExceptionCasePerson updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新额外人员信息表实体成功", exceptionCasePersonService.saveOrUpdate(updateReqVO));
    }

    @PostMapping("/batchUpdate")
    @Operation(summary = "批量更新额外人员信息")
    @AuditLog(value = "批量更新额外人员信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateExceptionCasePerson(@Valid @RequestBody List<ExceptionCasePerson> updateReqVOList) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新额外人员信息表实体成功", exceptionCasePersonService.updateBatchById(updateReqVOList));
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除额外人员信息表实体")
    @AuditLog(value = "删除额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteExceptionCasePerson(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除额外人员信息表实体成功", exceptionCasePersonService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出额外人员信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出额外人员信息表报表")
    public void exportExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        exceptionCasePersonService.exports(exportReqVO, response);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入额外人员信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入额外人员信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
    	// imports方法会导入保存数据到数据库，reads方法会从文件读取数据但不保存
        List<ExceptionCasePerson> importReqVO = exceptionCasePersonService.imports(file);
        return new ResponseResult<>(HttpStatus.OK.value(), "导入额外人员信息表报表成功", true);
    }

}
