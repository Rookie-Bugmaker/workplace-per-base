package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpSkillLevelServiceImpl;
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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * <p>
 * 职业技能等级 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpSkillLevel")
@RefreshScope
@Tag(name = "职业技能等级", description = "职业技能等级")
public class HrEmpSkillLevelController {

    @Autowired
    private HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;

    @PostMapping("/page")
    @Operation(summary = "获取职业技能等级列表 page")
    @AuditLog(value = "获取职业技能等级列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpSkillLevel(@Validated @RequestBody(required = false) HrEmpSkillLevel pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpSkillLevel() : pageReqVO;
        Page<HrEmpSkillLevel> page = new Page<>(pageNo, pageSize);
        hrEmpSkillLevelService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业技能等级列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取职业技能等级列表")
    @AuditLog(value = "获取职业技能等级列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpSkillLevel>> queryHrEmpSkillLevel(@Validated @RequestBody(required = false) HrEmpSkillLevel queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpSkillLevel() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业技能等级列表成功", hrEmpSkillLevelService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得职业技能等级实体")
    @AuditLog(value = "获得职业技能等级实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpSkillLevel> getHrEmpSkillLevel(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业技能等级实体成功", hrEmpSkillLevelService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建职业技能等级实体")
    @AuditLog(value = "创建职业技能等级实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpSkillLevel(@Valid @RequestBody HrEmpSkillLevel createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建职业技能等级实体成功", hrEmpSkillLevelService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新职业技能等级实体")
    @AuditLog(value = "更新职业技能等级实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpSkillLevel(@Valid @RequestBody HrEmpSkillLevel updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新职业技能等级实体成功", hrEmpSkillLevelService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除职业技能等级实体")
    @AuditLog(value = "删除职业技能等级实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpSkillLevel(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除职业技能等级实体成功", hrEmpSkillLevelService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出职业技能等级报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出职业技能等级报表")
    public void exportHrEmpSkillLevel(@Validated @RequestBody(required = false) HrEmpSkillLevel exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("职业技能等级报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpSkillLevel() : exportReqVO;
        List<HrEmpSkillLevel> hrEmpSkillLevelList = hrEmpSkillLevelService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpSkillLevel.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("职业技能等级报表")
                .doWrite(hrEmpSkillLevelList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入职业技能等级报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入职业技能等级报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpSkillLevel.class,
                new ImportExcelListener<HrEmpSkillLevel, HrEmpSkillLevelServiceImpl>(hrEmpSkillLevelService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入职业技能等级报表成功");
    }

}
