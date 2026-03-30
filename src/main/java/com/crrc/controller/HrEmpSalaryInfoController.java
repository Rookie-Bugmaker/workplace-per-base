package com.crrc.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.CostCenterListVO;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;
import com.crrc.utils.RoleUtils;
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
 * 员工薪资信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-08-26 15:23:52
 */
@RestController
@RequestMapping("/hrEmpSalaryInfo")
@RefreshScope
@Tag(name = "员工薪资信息表", description = "员工薪资信息表")
// TODO: 需要自然年+1！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！工龄标准
public class HrEmpSalaryInfoController {

    @Autowired
    private HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;

    @PostMapping("/getDepartmentCode")
    @Operation(summary = "获取员工成本中心")
    @AuditLog(value = "获取员工成本中心", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> getDepartmentCode(@Validated @RequestBody(required = false) CostCenterListVO pageReqVO,
                                                     @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                     @RequestParam(required = false, defaultValue = "10")int pageSize) {

        pageReqVO = (pageReqVO == null) ? new CostCenterListVO() : pageReqVO;
        try {
            Page<CostCenterListVO> page = new Page<>(pageNo, pageSize);
            IPage<CostCenterListVO> result = hrEmpSalaryInfoService.listData(page, pageReqVO);
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveCostCenterByUserCode")
    @Operation(summary = "根据档案号保存员工薪资信息中的成本中心实体")
    @AuditLog(value = "员工薪资信息表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> saveCostCenterByUserCode(@Valid @RequestBody CostCenterListVO reqVO) {

        LambdaQueryWrapper<HrEmpSalaryInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpSalaryInfo::getUserCode, reqVO.getUserCode());
        HrEmpSalaryInfo entity = new HrEmpSalaryInfo();
        entity.setUserCode(reqVO.getUserCode());
        entity.setDepartmentCode(reqVO.getCostcenterCode());
        boolean result = hrEmpSalaryInfoService.saveOrUpdate(entity, wrapper);

        return new ResponseResult<>(HttpStatus.OK.value(), "保存员工薪资信息表实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工薪资信息表列表 page")
    @AuditLog(value = "获取员工薪资信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpSalaryInfo(@Validated @RequestBody(required = false) HrEmpSalaryInfo pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpSalaryInfo() : pageReqVO;
        Page<HrEmpSalaryInfo> page = new Page<>(pageNo, pageSize);
        hrEmpSalaryInfoService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工薪资信息表列表 page成功", page);
    }

    @PostMapping("/addSalaryInfoSenYear")
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> addSalaryInfoSenYear(@RequestBody List<String> userCodes) {
        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
            LambdaQueryWrapper<HrEmpSalaryInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(HrEmpSalaryInfo::getUserCode, userCodes);
            List<HrEmpSalaryInfo> hrEmpSalaryInfos = hrEmpSalaryInfoService.getBaseMapper().selectList(queryWrapper);

            hrEmpSalaryInfos.forEach(item -> item.setSeniorityStandard(String.valueOf(Integer.parseInt(item.getSeniorityStandard()) + 1)));

            hrEmpSalaryInfoService.updateBatchById(hrEmpSalaryInfos);
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "OK");
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工薪资信息表列表")
    @AuditLog(value = "获取员工薪资信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpSalaryInfo>> queryHrEmpSalaryInfo(@Validated @RequestBody(required = false) HrEmpSalaryInfo queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpSalaryInfo() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工薪资信息表列表成功", hrEmpSalaryInfoService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工薪资信息表实体")
    @AuditLog(value = "获得员工薪资信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpSalaryInfo> getHrEmpSalaryInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工薪资信息表实体成功", hrEmpSalaryInfoService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/save")
    @Operation(summary = "保存员工薪资信息表实体")
    @AuditLog(value = "员工薪资信息表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> saveHrEmpSalaryInfo(@Valid @RequestBody HrEmpSalaryInfo reqVO) {
    
        boolean result = hrEmpSalaryInfoService.saveOrUpdate(reqVO);
    
        return new ResponseResult<>(HttpStatus.OK.value(), "保存员工薪资信息表实体成功", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveByUserCode")
    @Operation(summary = "根据档案号保存员工薪资信息表实体")
    @AuditLog(value = "员工薪资信息表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> saveByUserCode(@Valid @RequestBody HrEmpSalaryInfo reqVO) {
    
        LambdaQueryWrapper<HrEmpSalaryInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpSalaryInfo::getUserCode, reqVO.getUserCode());
        boolean result = hrEmpSalaryInfoService.saveOrUpdate(reqVO, wrapper);
    
        return new ResponseResult<>(HttpStatus.OK.value(), "保存员工薪资信息表实体成功", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工薪资信息表实体")
    @AuditLog(value = "删除员工薪资信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpSalaryInfo(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工薪资信息表实体成功", hrEmpSalaryInfoService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工薪资信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工薪资信息表报表")
    public void exportHrEmpSalaryInfo(@Validated @RequestBody(required = false) HrEmpSalaryInfo exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工薪资信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpSalaryInfo() : exportReqVO;
        List<HrEmpSalaryInfo> hrEmpSalaryInfoList = hrEmpSalaryInfoService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpSalaryInfo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工薪资信息表报表")
                .doWrite(hrEmpSalaryInfoList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工薪资信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工薪资信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpSalaryInfo.class,
                new ImportExcelListener<HrEmpSalaryInfo, HrEmpSalaryInfoServiceImpl>(hrEmpSalaryInfoService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工薪资信息表报表成功");
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工薪资信息表实体")
    @AuditLog(value = "获得员工薪资信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<HrEmpSalaryInfo>> getMsg(@RequestParam String userCode ) {
        List<HrEmpSalaryInfo> result = hrEmpSalaryInfoService.selectMsg(userCode);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表实体成功", result);
    }

}
