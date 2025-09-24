package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpProfessionalQualification;
import com.crrc.entity.HrEmpRewards;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.UpdateListDTO;
import com.crrc.service.impl.HrEmpProfessionalQualificationServiceImpl;
import com.crrc.service.impl.HrEmpRewardsServiceImpl;
import com.crrc.utils.UpdateListUtil;
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
 * 员工专业职务任职资格表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpProfessionalQualification")
@RefreshScope
@Tag(name = "员工专业职务任职资格表", description = "员工专业职务任职资格表")
public class HrEmpProfessionalQualificationController {

    @Autowired
    private HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService;

    private final UpdateListUtil<HrEmpProfessionalQualification, String> updateUtil;

    public HrEmpProfessionalQualificationController(HrEmpProfessionalQualificationServiceImpl service) {
        this.updateUtil = new UpdateListUtil<>(
                service::selectMsg,          // 查询方法
                HrEmpProfessionalQualification::getId,      // ID获取方法
                HrEmpProfessionalQualification::setUserCode,// userCode设置方法
                HrEmpProfessionalQualification::setId,      // ID设置方法
                service::saveOrUpdateBatch,  // 保存/更新方法
                service::removeByIds         // 删除方法
        );
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工专业职务任职资格实体")
    @AuditLog(value = "获得员工专业职务任职资格实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpProfessionalQualification>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工专业职务任职资格实体成功", hrEmpProfessionalQualificationService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工专业职务任职资格表列表 page")
    @AuditLog(value = "获取员工专业职务任职资格表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpProfessionalQualification(@Validated @RequestBody(required = false) HrEmpProfessionalQualification pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpProfessionalQualification() : pageReqVO;
        Page<HrEmpProfessionalQualification> page = new Page<>(pageNo, pageSize);
        hrEmpProfessionalQualificationService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工专业职务任职资格表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工专业职务任职资格表列表")
    @AuditLog(value = "获取员工专业职务任职资格表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpProfessionalQualification>> queryHrEmpProfessionalQualification(@Validated @RequestBody(required = false) HrEmpProfessionalQualification queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpProfessionalQualification() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工专业职务任职资格表列表成功", hrEmpProfessionalQualificationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工专业职务任职资格表实体")
    @AuditLog(value = "获得员工专业职务任职资格表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpProfessionalQualification> getHrEmpProfessionalQualification(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工专业职务任职资格表实体成功", hrEmpProfessionalQualificationService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工专业职务任职资格表实体")
    @AuditLog(value = "创建员工专业职务任职资格表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpProfessionalQualification(@Valid @RequestBody HrEmpProfessionalQualification createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工专业职务任职资格表实体成功", hrEmpProfessionalQualificationService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工专业职务任职资格表实体")
    @AuditLog(value = "更新员工专业职务任职资格表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpProfessionalQualification(@Valid @RequestBody UpdateListDTO<HrEmpProfessionalQualification> updateReqVO) {
        return updateUtil.update(updateReqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工专业职务任职资格表实体")
    @AuditLog(value = "删除员工专业职务任职资格表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpProfessionalQualification(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工专业职务任职资格表实体成功", hrEmpProfessionalQualificationService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工专业职务任职资格表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工专业职务任职资格表报表")
    public void exportHrEmpProfessionalQualification(@Validated @RequestBody(required = false) HrEmpProfessionalQualification exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工专业职务任职资格表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpProfessionalQualification() : exportReqVO;
        List<HrEmpProfessionalQualification> hrEmpProfessionalQualificationList = hrEmpProfessionalQualificationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpProfessionalQualification.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工专业职务任职资格表报表")
                .doWrite(hrEmpProfessionalQualificationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工专业职务任职资格表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工专业职务任职资格表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpProfessionalQualification.class,
                new ImportExcelListener<HrEmpProfessionalQualification, HrEmpProfessionalQualificationServiceImpl>(hrEmpProfessionalQualificationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工专业职务任职资格表报表成功");
    }

}
