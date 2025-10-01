package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkCoreAPi;
import com.crrc.entity.HrEmpCareerPath;
import com.crrc.entity.LoginUser;
import com.crrc.entity.dto.UserWithRoleDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpCareerPathDTO;
import com.crrc.pojo.dto.UpdateListDTO;
import com.crrc.service.impl.HrEmpCareerPathServiceImpl;
import com.crrc.utils.SecurityFrameworkUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 职业发展通道 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpCareerPath")
@RefreshScope
@Tag(name = "职业发展通道", description = "职业发展通道")
@Slf4j
public class HrEmpCareerPathController {

    @Autowired
    private HrEmpCareerPathServiceImpl hrEmpCareerPathService;
    @Autowired
    private WkCoreAPi wkCoreAPi;

    private final UpdateListUtil<HrEmpCareerPath, String> updateUtil;

    public HrEmpCareerPathController(HrEmpCareerPathServiceImpl service) {
        this.updateUtil = new UpdateListUtil<>(
                service::selectMsg,          // 查询方法
                HrEmpCareerPath::getId,      // ID获取方法
                HrEmpCareerPath::setUserCode,// userCode设置方法
                HrEmpCareerPath::setId,      // ID设置方法
                service::saveOrUpdateBatch,  // 保存/更新方法
                service::removeByIds         // 删除方法
        );
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得职业发展通道实体")
    @AuditLog(value = "获得职业发展通道实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpCareerPath>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业发展通道实体成功", hrEmpCareerPathService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取职业发展通道列表 page")
    @AuditLog(value = "获取职业发展通道列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpCareerPath(@Validated @RequestBody(required = false) HrEmpCareerPath pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpCareerPath() : pageReqVO;
        Page<HrEmpCareerPath> page = new Page<>(pageNo, pageSize);
        hrEmpCareerPathService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业发展通道列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取职业发展通道列表")
    @AuditLog(value = "获取职业发展通道列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpCareerPath>> queryHrEmpCareerPath(@Validated @RequestBody(required = false) HrEmpCareerPath queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpCareerPath() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业发展通道列表成功", hrEmpCareerPathService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得职业发展通道实体")
    @AuditLog(value = "获得职业发展通道实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpCareerPath> getHrEmpCareerPath(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业发展通道实体成功", hrEmpCareerPathService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建职业发展通道实体")
    @AuditLog(value = "创建职业发展通道实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpCareerPath(@Valid @RequestBody HrEmpCareerPath createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建职业发展通道实体成功", hrEmpCareerPathService.save(createReqVO));
    }


    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新职业发展通道实体")
    @AuditLog(value = "更新职业发展通道实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpCareerPath(@Valid @RequestBody HrEmpCareerPathDTO updateReqVO) {
        try {
            // 1. 获取当前用户并校验
            LoginUser currentUser = SecurityFrameworkUtils.getLoginUser();
            if (currentUser == null) {
                log.warn("获取当前登录用户失败!");
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "获取当前登录用户失败!");
            }
            log.info("当前操作用户ID: {}, 用户名: {}", currentUser.getUser().getId(), currentUser.getUser().getUserName());

            // 2. 权限校验 - 检查是否是人力资源部-人才管理专员
            log.info("开始权限校验，用户ID: {}", currentUser.getUser().getId());
            List<UserWithRoleDTO> usersWithRoles = wkCoreAPi.getUsersWithRoles(
                    Collections.singletonList(currentUser.getUser().getId()));

            boolean isHrTalentManager = usersWithRoles.stream()
                    .findFirst()
                    .map(UserWithRoleDTO::getRoles)
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(role -> "人力资源部-人才管理专员".equals(role.getName()));

            if (!isHrTalentManager) {
                log.warn("权限校验失败，用户缺少'人力资源部-人才管理专员'角色");
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "当前登录用户无权修改此信息!");
            }
            log.info("权限校验通过，用户拥有'人力资源部-人才管理专员'角色");

            // 3. 业务处理 - 增删改三合一
            return processHrEmpCareerPathUpdate(updateReqVO);
        } catch (Exception e) {
            log.error("更新职业发展通道时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新职业发展通道实体失败");
        }
    }

    /**
     * 处理职业发展通道更新逻辑（增删改三合一）
     */
    private ResponseResult<Boolean> processHrEmpCareerPathUpdate(HrEmpCareerPathDTO updateReqVO) {
        log.info("开始处理职业发展通道更新，员工编码: {}", updateReqVO.getUserCode());

        // 查询现有数据
        List<HrEmpCareerPath> existList = hrEmpCareerPathService.selectMsg(updateReqVO.getUserCode());
        Set<String> existIds = existList.stream()
                .map(HrEmpCareerPath::getId)
                .collect(Collectors.toSet());
        log.info("查询到现有记录数量: {}", existList.size());

        // 准备新增/更新的数据
        List<HrEmpCareerPath> toSaveOrUpdate = new ArrayList<>();
        int newCount = 0, updateCount = 0;

        for (HrEmpCareerPath careerPath : updateReqVO.getList()) {
            // 生成ID（新增记录）
            if (careerPath.getId() == null) {
                careerPath.setId(IdUtil.simpleUUID());
                newCount++;
            } else {
                updateCount++;
            }

            // 设置员工编码
            careerPath.setUserCode(updateReqVO.getUserCode());
            toSaveOrUpdate.add(careerPath);

            // 从待删除集合中移除当前ID
            existIds.remove(careerPath.getId());
        }
        log.info("待处理记录 - 新增: {}, 更新: {}", newCount, updateCount);

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            log.info("开始批量保存/更新 {} 条记录", toSaveOrUpdate.size());
            hrEmpCareerPathService.saveOrUpdateBatch(toSaveOrUpdate);
        }

        // 批量删除（本次未传入的记录）
        if (!existIds.isEmpty()) {
            log.info("开始批量删除 {} 条记录", existIds.size());
            hrEmpCareerPathService.removeByIds(existIds);
        }

        log.info("职业发展通道更新处理完成");
        return new ResponseResult<>(HttpStatus.OK.value(), "更新职业发展通道实体成功", true);
    }


    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除职业发展通道实体")
    @AuditLog(value = "删除职业发展通道实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpCareerPath(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除职业发展通道实体成功", hrEmpCareerPathService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出职业发展通道报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出职业发展通道报表")
    public void exportHrEmpCareerPath(@Validated @RequestBody(required = false) HrEmpCareerPath exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("职业发展通道报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpCareerPath() : exportReqVO;
        List<HrEmpCareerPath> hrEmpCareerPathList = hrEmpCareerPathService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpCareerPath.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("职业发展通道报表")
                .doWrite(hrEmpCareerPathList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入职业发展通道报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入职业发展通道报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpCareerPath.class,
                new ImportExcelListener<HrEmpCareerPath, HrEmpCareerPathServiceImpl>(hrEmpCareerPathService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入职业发展通道报表成功");
    }

}
