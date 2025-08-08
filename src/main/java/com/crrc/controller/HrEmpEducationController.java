package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpEducation;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpEducationDTO;
import com.crrc.service.impl.HrEmpEducationServiceImpl;
import com.crrc.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 员工教育信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpEducation")
@RefreshScope
@Tag(name = "员工教育信息表", description = "员工教育信息表")
public class HrEmpEducationController {

    @Autowired
    private HrEmpEducationServiceImpl hrEmpEducationService;

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工教育信息表实体")
    @AuditLog(value = "获得员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpEducation>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表实体成功", hrEmpEducationService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工教育信息表列表 page")
    @AuditLog(value = "获取员工教育信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage> pageHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpEducation() : pageReqVO;
        Page<HrEmpEducation> page = new Page<>(pageNo, pageSize);
        hrEmpEducationService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工教育信息表列表")
    @AuditLog(value = "获取员工教育信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpEducation>> queryHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpEducation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表列表成功", hrEmpEducationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工教育信息表实体")
    @AuditLog(value = "获得员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpEducation> getHrEmpEducation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表实体成功", hrEmpEducationService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工教育信息表实体")
    @AuditLog(value = "创建员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpEducation(@Valid @RequestBody HrEmpEducation createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工教育信息表实体成功", hrEmpEducationService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工教育信息表实体")
    @AuditLog(value = "更新员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpEducation(@Valid @RequestBody HrEmpEducationDTO updateReqVO) {
        try {
            // 查询现有数据
            List<HrEmpEducation> existList = hrEmpEducationService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpEducation::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpEducation> toSaveOrUpdate = new ArrayList<>();

            // 判断传入数据里面有无最高学历和学位
            boolean isHighestEdu = true;
            int isHighestNum = 0;
            for (HrEmpEducation hrEmpEducation : updateReqVO.getList()) {
                if (hrEmpEducation.getId() == null) {
                    hrEmpEducation.setId(IdUtil.simpleUUID()); // 生成新ID
                }
                // 判断最高学历
                if(SystemConstants.TRUE_FLAG.equals(hrEmpEducation.getIsHighestEducation())) {
                    isHighestNum++;
                    isHighestEdu = false;
                }
                // 当最高学历数量多余1时
                if(isHighestNum > 1) {
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "最高学历数量不唯一！");
                }
                hrEmpEducation.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(hrEmpEducation);
                existIds.remove(hrEmpEducation.getId()); // 移除不需要删除的ID
            }
            // 没有最高学历,且教育信息列表不为空时
            if(isHighestEdu && !updateReqVO.getList().isEmpty()) {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "录入教育经历中未设置最高学历！");
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpEducationService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpEducationService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工教育信息表实体失败");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工教育信息表实体成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工教育信息表实体")
    @AuditLog(value = "删除员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpEducation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工教育信息表实体成功", hrEmpEducationService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工教育信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工教育信息表报表")
    public void exportHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工教育信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpEducation() : exportReqVO;
        List<HrEmpEducation> hrEmpEducationList = hrEmpEducationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpEducation.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工教育信息表报表")
                .doWrite(hrEmpEducationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工教育信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工教育信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpEducation.class,
                new ImportExcelListener<HrEmpEducation, HrEmpEducationServiceImpl>(hrEmpEducationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工教育信息表报表成功");
    }

}
