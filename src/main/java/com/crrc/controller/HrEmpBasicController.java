package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpHealth;
import com.crrc.entity.HrEmpHouseHold;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import com.crrc.service.impl.HrEmpHealthServiceImpl;
import com.crrc.service.impl.HrEmpHouseholdServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
 * 员工基本信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpBasic")
@RefreshScope
@Tag(name = "员工基本信息表", description = "员工基本信息表")
public class HrEmpBasicController {

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;
    @Autowired
    private HrEmpHealthServiceImpl hrEmpHealthService;

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工基本信息表列表 page")
    @AuditLog(value = "获取员工基本信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasic pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpBasic() : pageReqVO;
        Page<HrEmpBasic> page = new Page<>(pageNo, pageSize);
        hrEmpBasicService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工基本信息表列表")
    @AuditLog(value = "获取员工基本信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpBasic>> queryHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasic queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpBasic() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表列表成功", hrEmpBasicService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpBasic> getHrEmpBasic(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工基本信息表实体")
    @AuditLog(value = "创建员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpBasic(@Valid @RequestBody HrEmpBasic createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpBasicService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工基本信息表实体")
    @AuditLog(value = "更新员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpBasic(@Valid @RequestBody HrEmpBasicDTO updateReqVO) {
        //基本信息中，健康状况，户口相关需要跨表保存
        try{
            //健康信息部分
            String userCode = updateReqVO.getUserCode();
            HrEmpHealth hrEmpHealth = hrEmpHealthService.selectByUserCode(userCode);
            if(hrEmpHealth == null) {
                HrEmpHealth saveEntity = new HrEmpHealth();
                saveEntity.setUserCode(userCode);
                saveEntity.setHealthStatus(updateReqVO.getHealthStatus());
                saveEntity.setId(IdUtil.simpleUUID());
                hrEmpHealthService.save(saveEntity);
            } else {
                hrEmpHealth.setHealthStatus(updateReqVO.getHealthStatus());
                hrEmpHealthService.saveOrUpdate(hrEmpHealth);
            }

            //户口信息部分
            HrEmpHouseHold hrEmpHousehold = hrEmpHouseholdService.selectByUserCode(userCode);
            if (hrEmpHousehold == null) {
                HrEmpHouseHold saveEntity = new HrEmpHouseHold();
                saveEntity.setUserCode(userCode);
                saveEntity.setId(IdUtil.simpleUUID());
                saveEntity.setHouseHoldLocation(updateReqVO.getHouseHoldLocation());
                saveEntity.setHouseHoldLocationDetail(updateReqVO.getHouseHoldLocationDetail());
                saveEntity.setHouseHoldType(updateReqVO.getHouseHoldType());
                hrEmpHouseholdService.save(saveEntity);
            } else {
                hrEmpHousehold.setHouseHoldLocation(updateReqVO.getHouseHoldLocation());
                hrEmpHousehold.setHouseHoldLocationDetail(updateReqVO.getHouseHoldLocationDetail());
                hrEmpHousehold.setHouseHoldType(updateReqVO.getHouseHoldType());
                hrEmpHouseholdService.saveOrUpdate(hrEmpHousehold);
            }
            if(updateReqVO.getId() == null){
                updateReqVO.setId(IdUtil.simpleUUID());
                hrEmpBasicService.save(updateReqVO);
            }
            hrEmpBasicService.saveOrUpdate(updateReqVO);
        } catch (Exception e) {
            log.error("更新或保存时出错：\n" +"入参： "+ updateReqVO + "\n" + "错误日志： " + e.getMessage() + "\n");
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "更新员工教育信息表实体失败");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工基本信息表实体")
    @AuditLog(value = "删除员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpBasic(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工基本信息表实体成功", hrEmpBasicService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工基本信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工基本信息表报表")
    public void exportHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasic exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工基本信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpBasic() : exportReqVO;
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpBasic.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工基本信息表报表")
                .doWrite(hrEmpBasicList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工基本信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工基本信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpBasic.class,
                new ImportExcelListener<HrEmpBasic, HrEmpBasicServiceImpl>(hrEmpBasicService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工基本信息表报表成功");
    }

}
