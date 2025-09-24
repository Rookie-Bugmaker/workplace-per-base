package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpHealth;
import com.crrc.entity.HrEmpHouseHold;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mongoDB.HrEmpChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.query.Query;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    // 流程模板编码，在框架界面中规定
    private static final String process_templateAlias = "userinfo";

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;
    @Autowired
    private HrEmpHealthServiceImpl hrEmpHealthService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;


    @GetMapping("/getDetails")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsg(@RequestParam String userCode) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工基本信息表列表 page")
    @AuditLog(value = "获取员工基本信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<HrEmpBasicDTO>> pageHrEmpBasic(@Validated @RequestBody(required = false) HrEmpBasicSearchDTO pageReqVO,
                                                               @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                               @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpBasicSearchDTO() : pageReqVO;
        try {
            Page<HrEmpBasicDTO> page = new Page<>(pageNo, pageSize);
            IPage<HrEmpBasicDTO> result = hrEmpBasicService.listData(page, pageReqVO);
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工基本信息表列表")
    @AuditLog(value = "获取员工基本信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
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


    @PostMapping("/submit-change")
    @Operation(summary = "提交员工信息变更申请")
    public ResponseResult<String> submitChange(@Valid @RequestBody HrEmpBasicDTO changeReqVO) {
        try {
            // 1. 获取当前数据快照
            HrEmpBasicDTO originalData = hrEmpBasicService.selectMsg(changeReqVO.getUserCode()).get(0);
            if (originalData == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "员工信息不存在");
            }

            // 2. 创建变更申请文档
            HrEmpChangeRequest changeRequest = new HrEmpChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);
            changeRequest.setChangedData(changeReqVO);
            changeRequest.setWfId(changeReqVO.getWfId());

            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);
            log.info("流程测试changeRequest" + changeRequest);


            // 4. 启动审批流程
            // 封装数据，提交审批流
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            instanceDTO.setBizCode(changeReqVO.getId());
            // 封装数据主键，存在MongoDB里面的生成的simpleUUID
//            String bizId = "BASIC-" + System.currentTimeMillis();
            instanceDTO.setBizId(changeRequest.getId());
            // 封装业务模板编码
            instanceDTO.setTemplateAlias(process_templateAlias);
            // 封装业务表单数据，20250924上线暂时没用，如果之后需要修改审批流分支，需使用该数据
            Map<String, Object> formData = new HashMap<>();
            formData.put("Basic",convertToMapList(changeReqVO));
            instanceDTO.setFormData(formData);
            log.info("流程测试instanceDTO" + instanceDTO);
            // 返回流程实例id
            ProcessInstanceVo processInstanceVo = wkWorkflowApi.startProcessInstanceNew(instanceDTO);
            log.info("流程测试processInstanceVo" + processInstanceVo);

            // 5. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getProcessInstanceId());
            mongoTemplate.save(changeRequest);
            log.info("流程测试success");
            return new ResponseResult<>(HttpStatus.OK.value(), "变更申请已提交", changeRequest.getId());
        } catch (Exception e) {
            log.error("提交变更申请失败: {}", e.getMessage());
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        // 1. 从MongoDB获取变更请求
        HrEmpChangeRequest request = mongoTemplate.findById(requestId, HrEmpChangeRequest.class);
        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
        }

        // 2. 构建返回结果（直接返回两个列表）
        Map<String, Object> result = new HashMap<>();

        // 原始数据列表（转换为Map列表或直接使用对象）
        result.put("originalData", convertToMapList(request.getOriginalData()));

        // 审批流中的数据列表
        result.put("changedData", convertToMapList(request.getChangedData()));

        // 基础信息
        result.put("requestId", requestId);
        result.put("createTime", request.getCreateTime());

        return new ResponseResult<>(HttpStatus.OK.value(), "成功", result);
    }

    // 辅助方法：将数据对象转换为Map（可根据实际需求调整）
    private List<Map<String, Object>> convertToMapList(Object data) {
        // 方案1：如果数据本身就是集合
        if (data instanceof Collection) {
            return ((Collection<?>) data).stream()
                    .map(this::convertSingleObjectToMap)
                    .collect(Collectors.toList());
        }

        // 方案2：如果是单个对象
        return Collections.singletonList(convertSingleObjectToMap(data));
    }

    // 将单个对象转为Map
    private Map<String, Object> convertSingleObjectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }

        try {
            return Arrays.stream(BeanUtils.getPropertyDescriptors(obj.getClass()))
                    .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                    .collect(HashMap::new,
                            (map, pd) -> {
                                try {
                                    Object value = pd.getReadMethod().invoke(obj);
                                    if (value != null) {  // 显式过滤null值
                                        map.put(pd.getName(), value);
                                    }
                                } catch (Exception ignored) {}
                            },
                            HashMap::putAll);
        } catch (Exception e) {
            throw new RuntimeException("对象转换Map失败", e);
        }
    }

    /**
     * 回调接口
     * @param param
     * @return
     */
    @PutMapping("/approval-callback")
    @IgnoreTenant
    public ResponseResult<Boolean> approvalCallback(@RequestBody WorkflowWriteBackParamDTO param) {
        try {
            // 1. 获取变更请求
            Query query = new Query(Criteria.where("_id").is(param.getBizId()));
            HrEmpChangeRequest request = mongoTemplate.findOne(query, HrEmpChangeRequest.class);


            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在", false);
            }

            String userCode = request.getChangedData().getUserCode();
            String isUpdatingValue = "1"; // 默认值

            // 2. 处理不同审批状态
            if (param.getProcessInstanceStatus() == 2) { // 审批通过
                if ("APPROVED".equals(request.getStatus())) {
                    return new ResponseResult<>(HttpStatus.OK.value(), "已处理过该申请", true);
                }

                // 执行实际更新
                updateHrEmpBasic(request.getChangedData());

                // 更新状态
                request.setStatus("APPROVED");
                isUpdatingValue = "0"; // 审批通过时设为0

            } else if (param.getProcessInstanceStatus() == 3) { // 审批拒绝
                request.setStatus("REJECTED");
                isUpdatingValue = "0"; // 审批拒绝时也设为0
            }

            mongoTemplate.save(request);
            updateIsUpdating(isUpdatingValue, userCode);

            return new ResponseResult<>(HttpStatus.OK.value(), "处理成功", true);
        } catch (Exception e) {
            log.error("审批回调处理失败: {}", e.getMessage());
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "处理失败", false);
        }
    }

    // 变更是否被更新的状态
    private void updateIsUpdating(String isUpdating, String userCode) {
        HrEmpBasic temp = hrEmpBasicService.selectMsg(userCode).get(0);
        if(temp == null) {
            return;
        }
        temp.setIsUpdating(isUpdating);
        hrEmpBasicService.updateById(temp);
    }


    // 实际更新方法
    private void updateHrEmpBasic(HrEmpBasicDTO updateReqVO) {
        String userCode = updateReqVO.getUserCode();

        // 健康信息部分
        HrEmpHealth hrEmpHealth = hrEmpHealthService.selectByUserCode(userCode);
        if (hrEmpHealth == null) {
            HrEmpHealth saveEntity = new HrEmpHealth();
            saveEntity.setUserCode(userCode);
            saveEntity.setHealthStatus(updateReqVO.getHealthStatus());
            saveEntity.setId(IdUtil.simpleUUID());
            hrEmpHealthService.save(saveEntity);
        } else {
            hrEmpHealth.setHealthStatus(updateReqVO.getHealthStatus());
            hrEmpHealthService.saveOrUpdate(hrEmpHealth);
        }

        // 户口信息部分
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

        // 基本信息
        if (updateReqVO.getId() == null) {
            updateReqVO.setId(IdUtil.simpleUUID());
            hrEmpBasicService.save(updateReqVO);
        }
        hrEmpBasicService.updateById(updateReqVO);
    }




//    @Transactional(rollbackFor = Exception.class)
//    @PostMapping("/update")
//    @Operation(summary = "更新员工基本信息表实体")
//    @AuditLog(value = "更新员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
//    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
//    public ResponseResult<Boolean> updateHrEmpBasic(@Valid @RequestBody HrEmpBasicDTO updateReqVO) {
//        //基本信息中，健康状况，户口相关需要跨表保存
//        try {
//            //健康信息部分
//            String userCode = updateReqVO.getUserCode();
//            HrEmpHealth hrEmpHealth = hrEmpHealthService.selectByUserCode(userCode);
//            if (hrEmpHealth == null) {
//                HrEmpHealth saveEntity = new HrEmpHealth();
//                saveEntity.setUserCode(userCode);
//                saveEntity.setHealthStatus(updateReqVO.getHealthStatus());
//                saveEntity.setId(IdUtil.simpleUUID());
//                hrEmpHealthService.save(saveEntity);
//            } else {
//                hrEmpHealth.setHealthStatus(updateReqVO.getHealthStatus());
//                hrEmpHealthService.saveOrUpdate(hrEmpHealth);
//            }
//
//            //户口信息部分
//            HrEmpHouseHold hrEmpHousehold = hrEmpHouseholdService.selectByUserCode(userCode);
//            if (hrEmpHousehold == null) {
//                HrEmpHouseHold saveEntity = new HrEmpHouseHold();
//                saveEntity.setUserCode(userCode);
//                saveEntity.setId(IdUtil.simpleUUID());
//                saveEntity.setHouseHoldLocation(updateReqVO.getHouseHoldLocation());
//                saveEntity.setHouseHoldLocationDetail(updateReqVO.getHouseHoldLocationDetail());
//                saveEntity.setHouseHoldType(updateReqVO.getHouseHoldType());
//                hrEmpHouseholdService.save(saveEntity);
//            } else {
//                hrEmpHousehold.setHouseHoldLocation(updateReqVO.getHouseHoldLocation());
//                hrEmpHousehold.setHouseHoldLocationDetail(updateReqVO.getHouseHoldLocationDetail());
//                hrEmpHousehold.setHouseHoldType(updateReqVO.getHouseHoldType());
//                hrEmpHouseholdService.saveOrUpdate(hrEmpHousehold);
//            }
//            if (updateReqVO.getId() == null) {
//                updateReqVO.setId(IdUtil.simpleUUID());
//                hrEmpBasicService.save(updateReqVO);
//            }
//            hrEmpBasicService.saveOrUpdate(updateReqVO);
//        } catch (Exception e) {
//            log.error("更新或保存时出错：\n" + "入参： " + updateReqVO + "\n" + "错误日志： " + e.getMessage() + "\n");
//            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "更新员工教育信息表实体失败");
//        }
//        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功");
//    }

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
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工基本信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        exportReqVO = (exportReqVO == null) ? new HrEmpBasic() : exportReqVO;
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
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
