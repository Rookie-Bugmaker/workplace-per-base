package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.HrEmpSkillLevel;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mongodb.HrEmpSkillsChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.UpdateListDTO;
import com.crrc.service.impl.HrEmpSkillLevelServiceImpl;
import com.crrc.utils.UpdateListUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.utils.MyUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

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
@Slf4j
public class HrEmpSkillLevelController {

    @Autowired
    private HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;

    private final UpdateListUtil<HrEmpSkillLevel, String> updateUtil;

    public HrEmpSkillLevelController(HrEmpSkillLevelServiceImpl service) {
        this.updateUtil = new UpdateListUtil<>(
                service::selectMsg,          // 查询方法
                HrEmpSkillLevel::getId,      // ID获取方法
                HrEmpSkillLevel::setUserCode,// userCode设置方法
                HrEmpSkillLevel::setId,      // ID设置方法
                service::saveOrUpdateBatch,  // 保存/更新方法
                service::removeByIds         // 删除方法
        );
    }


    @GetMapping("/getDetails")
    @Operation(summary = "获得职业技能等级实体")
    @AuditLog(value = "获得职业技能等级实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpSkillLevel>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取职业技能等级成功", hrEmpSkillLevelService.selectMsg(userCode));
    }

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
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpSkillLevel(@Valid @RequestBody UpdateListDTO<HrEmpSkillLevel> updateReqVO) {
        return updateUtil.update(updateReqVO);
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

    @PostMapping("/submit-change")
    @Operation(summary = "提交职业技能等级变更申请")
    public ResponseResult<String> submitSkillChange(@Valid @RequestBody UpdateListDTO<HrEmpSkillLevel> changeReqVO) {
        try {
            // 1. 获取当前数据快照
            List<HrEmpSkillLevel> originalData = hrEmpSkillLevelService.selectMsg(changeReqVO.getUserCode());
            log.info("职业技能等级原始数据：{}", JSON.toJSONString(originalData));

            // 2. 创建变更申请文档
            HrEmpSkillsChangeRequest changeRequest = new HrEmpSkillsChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);
            changeRequest.setChangedData(changeReqVO.getList());
            changeRequest.setStatus("PENDING");

            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);

            // 4. 启动审批流程
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "SKL-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            instanceDTO.setBizId("skl_" + changeRequest.getId());
            instanceDTO.setTemplateAlias("skill_level_change"); // 替换为实际模板别名

            Map<String, Object> formData = new HashMap<>();
            formData.put("type", "skills");
            formData.put("SkillLevels", MyUtils.convertToMapList(changeReqVO.getList()));
            instanceDTO.setFormData(formData);

            ProcessInstanceVo processInstanceVo = wkWorkflowApi.startProcessInstanceNew(instanceDTO);

            // 5. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getProcessInstanceId());
            mongoTemplate.save(changeRequest);

            return new ResponseResult<>(HttpStatus.OK.value(), "职业技能等级变更申请已提交", changeRequest.getId());
        } catch (Exception e) {
            log.error("提交职业技能等级变更申请失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    // 获取变更详情的Controller方法
    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getSkillChangeDetail(@RequestParam String requestId) {
        try {
            requestId = MyUtils.subPk(requestId);
            HrEmpSkillsChangeRequest request = mongoTemplate.findById(requestId, HrEmpSkillsChangeRequest.class);

            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("originalData", MyUtils.convertToMapList(request.getOriginalData()));
            result.put("changedData", MyUtils.convertToMapList(request.getChangedData()));
            result.put("requestId", requestId);
            result.put("createTime", request.getCreateTime());
            result.put("status", request.getStatus());

            return new ResponseResult<>(HttpStatus.OK.value(), "成功", result);
        } catch (Exception e) {
            log.error("获取职业技能等级变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }

}
