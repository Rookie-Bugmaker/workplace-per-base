package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkCoreAPi;
import com.crrc.entity.HrEmpPenalties;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpPenaltiesDTO;
import com.crrc.service.impl.HrEmpPenaltiesServiceImpl;
import com.crrc.utils.RoleUtils;
import com.crrc.utils.UpdateListUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 员工惩处信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpPenalties")
@RefreshScope
@Tag(name = "员工惩处信息表", description = "员工惩处信息表")
@Slf4j
public class HrEmpPenaltiesController {

    @Autowired
    private HrEmpPenaltiesServiceImpl hrEmpPenaltiesService;
    @Autowired
    private WkCoreAPi wkCoreAPi;

    private final UpdateListUtil<HrEmpPenalties, String> updateUtil;

    public HrEmpPenaltiesController(HrEmpPenaltiesServiceImpl service) {
        this.updateUtil = new UpdateListUtil<>(
                service::selectMsg,          // 查询方法
                HrEmpPenalties::getId,      // ID获取方法
                HrEmpPenalties::setUserCode,// userCode设置方法
                HrEmpPenalties::setId,      // ID设置方法
                service::saveOrUpdateBatch,  // 保存/更新方法
                service::removeByIds         // 删除方法
        );
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工惩处信息实体")
    @AuditLog(value = "获得员工惩处信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpPenalties>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工惩处信息实体成功", hrEmpPenaltiesService.selectMsg(userCode));
    }


    @PostMapping("/page")
    @Operation(summary = "获取员工惩处信息表列表 page")
    @AuditLog(value = "获取员工惩处信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpPenalties(@Validated @RequestBody(required = false) HrEmpPenalties pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpPenalties() : pageReqVO;
        Page<HrEmpPenalties> page = new Page<>(pageNo, pageSize);
        hrEmpPenaltiesService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工惩处信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工惩处信息表列表")
    @AuditLog(value = "获取员工惩处信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpPenalties>> queryHrEmpPenalties(@Validated @RequestBody(required = false) HrEmpPenalties queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpPenalties() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工惩处信息表列表成功", hrEmpPenaltiesService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工惩处信息表实体")
    @AuditLog(value = "获得员工惩处信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpPenalties> getHrEmpPenalties(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工惩处信息表实体成功", hrEmpPenaltiesService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工惩处信息表实体")
    @AuditLog(value = "创建员工惩处信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpPenalties(@Valid @RequestBody HrEmpPenalties createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工惩处信息表实体成功", hrEmpPenaltiesService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工惩处信息表实体")
    @AuditLog(value = "更新员工惩处信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpPenalties(@Valid @RequestBody HrEmpPenaltiesDTO updateReqVO) {
        try {
            boolean isHrPersonnelManager = RoleUtils.check("人力资源部-人事管理专员");
            if (!isHrPersonnelManager) {
                log.warn("权限校验失败，用户缺少'人力资源部-人事管理专员'角色");
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "当前登录用户无权修改惩处信息!");
            }
            log.info("权限校验通过，用户拥有'人力资源部-人事管理专员'角色");
            // 3. 业务处理 - 增删改三合一
            return processHrEmpPenaltiesUpdate(updateReqVO);
        } catch (Exception e) {
            log.error("更新惩处信息时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工惩处信息表实体失败");
        }
    }

    /**
     * 处理惩处信息更新逻辑（增删改三合一）
     */
    private ResponseResult<Boolean> processHrEmpPenaltiesUpdate(HrEmpPenaltiesDTO updateReqVO) {
        log.info("开始处理惩处信息更新，员工编码: {}", updateReqVO.getUserCode());

        // 查询现有数据
        List<HrEmpPenalties> existList = hrEmpPenaltiesService.selectMsg(updateReqVO.getUserCode());
        Set<String> existIds = existList.stream()
                .map(HrEmpPenalties::getId)
                .collect(Collectors.toSet());
        log.info("查询到现有惩处记录数量: {}", existList.size());

        // 准备新增/更新的数据
        List<HrEmpPenalties> toSaveOrUpdate = new ArrayList<>();
        int newCount = 0, updateCount = 0;

        for (HrEmpPenalties penalty : updateReqVO.getList()) {
            // 生成ID（新增记录）
            if (penalty.getId() == null) {
                penalty.setId(IdUtil.simpleUUID());
                newCount++;
            } else {
                updateCount++;
            }

            // 设置员工编码
            penalty.setUserCode(updateReqVO.getUserCode());
            toSaveOrUpdate.add(penalty);

            // 从待删除集合中移除当前ID
            existIds.remove(penalty.getId());
        }
        log.info("待处理记录 - 新增: {}, 更新: {}", newCount, updateCount);

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            log.info("开始批量保存/更新 {} 条惩处记录", toSaveOrUpdate.size());
            hrEmpPenaltiesService.saveOrUpdateBatch(toSaveOrUpdate);
        }

        // 批量删除（本次未传入的记录）
        if (!existIds.isEmpty()) {
            log.info("开始批量删除 {} 条惩处记录", existIds.size());
            hrEmpPenaltiesService.removeByIds(existIds);
        }

        log.info("惩处信息更新处理完成");
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工惩处信息表实体成功", true);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工惩处信息表实体")
    @AuditLog(value = "删除员工惩处信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpPenalties(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工惩处信息表实体成功", hrEmpPenaltiesService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工惩处信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工惩处信息表报表")
    public void exportHrEmpPenalties(@Validated @RequestBody(required = false) HrEmpPenalties exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工惩处信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpPenalties() : exportReqVO;
        List<HrEmpPenalties> hrEmpPenaltiesList = hrEmpPenaltiesService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpPenalties.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工惩处信息表报表")
                .doWrite(hrEmpPenaltiesList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工惩处信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工惩处信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpPenalties.class,
                new ImportExcelListener<HrEmpPenalties, HrEmpPenaltiesServiceImpl>(hrEmpPenaltiesService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工惩处信息表报表成功");
    }

}
