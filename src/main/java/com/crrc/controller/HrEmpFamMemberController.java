package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpFamMember;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpFamMemberDTO;
import com.crrc.service.impl.HrEmpFamMemberServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 员工主要家庭成员社会关系表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpFamMember")
@RefreshScope
@Tag(name = "员工主要家庭成员社会关系表", description = "员工主要家庭成员社会关系表")
@Slf4j
public class HrEmpFamMemberController {

    @Autowired
    private HrEmpFamMemberServiceImpl hrEmpFamMemberService;


    @GetMapping("/getDetails")
    @Operation(summary = "获得员工主要家庭成员社会关系表实体")
    @AuditLog(value = "获得员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpFamMember>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工主要家庭成员社会关系表实体成功", hrEmpFamMemberService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工主要家庭成员社会关系表列表 page")
    @AuditLog(value = "获取员工主要家庭成员社会关系表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpFamMember(@Validated @RequestBody(required = false) HrEmpFamMember pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpFamMember() : pageReqVO;
        Page<HrEmpFamMember> page = new Page<>(pageNo, pageSize);
        hrEmpFamMemberService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工主要家庭成员社会关系表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工主要家庭成员社会关系表列表")
    @AuditLog(value = "获取员工主要家庭成员社会关系表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpFamMember>> queryHrEmpFamMember(@Validated @RequestBody(required = false) HrEmpFamMember queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpFamMember() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工主要家庭成员社会关系表列表成功", hrEmpFamMemberService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工主要家庭成员社会关系表实体")
    @AuditLog(value = "获得员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpFamMember> getHrEmpFamMember(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工主要家庭成员社会关系表实体成功", hrEmpFamMemberService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工主要家庭成员社会关系表实体")
    @AuditLog(value = "创建员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpFamMember(@Valid @RequestBody HrEmpFamMember createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工主要家庭成员社会关系表实体成功", hrEmpFamMemberService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工主要家庭成员社会关系表实体")
    @AuditLog(value = "更新员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpFamMember(@Valid @RequestBody HrEmpFamMemberDTO updateReqVO) {
        try {
            // 查询现有数据
            List<HrEmpFamMember> existList = hrEmpFamMemberService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpFamMember::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpFamMember> toSaveOrUpdate = new ArrayList<>();

            // 判断传入数据里面有无紧急联系人
            boolean isEmergency = true;
            int emergencyNum = 0;
            for (HrEmpFamMember hrEmpFamMember : updateReqVO.getList()) {
                if (hrEmpFamMember.getId() == null) {
                    hrEmpFamMember.setId(IdUtil.simpleUUID()); // 生成新ID
                }
                // 判断紧急联系人
                if(SystemConstants.TRUE_FLAG.equals(hrEmpFamMember.getIsEmergencyContact())) {
                    emergencyNum++;
                    isEmergency = false;
                }
                // 当紧急联系人数量多余1时
                if(emergencyNum > 1) {
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "紧急联系人数量不唯一！");
                }
                hrEmpFamMember.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(hrEmpFamMember);
                existIds.remove(hrEmpFamMember.getId()); // 移除不需要删除的ID
            }
            // 没有紧急联系人,且家庭成员列表不为空时
            if(isEmergency && !updateReqVO.getList().isEmpty()) {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "录入员工主要家庭成员中未设置紧急联系人！");
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpFamMemberService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpFamMemberService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工主要家庭成员社会关系表实体失败");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工主要家庭成员社会关系表实体成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工主要家庭成员社会关系表实体")
    @AuditLog(value = "删除员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpFamMember(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工主要家庭成员社会关系表实体成功", hrEmpFamMemberService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工主要家庭成员社会关系表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工主要家庭成员社会关系表报表")
    public void exportHrEmpFamMember(@Validated @RequestBody(required = false) HrEmpFamMember exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工主要家庭成员社会关系表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpFamMember() : exportReqVO;
        List<HrEmpFamMember> hrEmpFamMemberList = hrEmpFamMemberService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpFamMember.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工主要家庭成员社会关系表报表")
                .doWrite(hrEmpFamMemberList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工主要家庭成员社会关系表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工主要家庭成员社会关系表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpFamMember.class,
                new ImportExcelListener<HrEmpFamMember, HrEmpFamMemberServiceImpl>(hrEmpFamMemberService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工主要家庭成员社会关系表报表成功");
    }

}
