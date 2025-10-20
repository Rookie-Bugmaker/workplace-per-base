package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.*;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mongodb.HrEmpBasicChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.service.impl.*;
import com.crrc.utils.MyUtils;
import com.crrc.utils.RoleUtils;
import com.crrc.utils.SystemConstants;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    @Autowired
    private HrEmpCommunicationServiceImpl hrEmpCommunicationService;
    @Autowired
    private HrEmpCertificateServiceImpl hrEmpCertificateService;
    @Autowired
    private HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;

    @PostMapping("/syncPersonCategory")
    @Operation(summary = "同步人员分类，目前用定时任务每天处理")
    @AuditLog(value = "同步人员分类，目前用定时任务每天处理", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<String> syncPersonCategory(@RequestBody(required = false) List<HrEmpBasic> userCodeList) {
        try {
            // 如果传参了且非空，就用传参，否则全量更新
            List<HrEmpBasic> list = (userCodeList != null && !userCodeList.isEmpty())
                    ? userCodeList
                    : hrEmpBasicService.getAllUser();

            for(HrEmpBasic hrEmpBasic:list){
                // 同步人员分类1，根据基本信息的身份类型判断人员分类1，
                // 员工基本信息→身份类型→干部  干
                // 员工基本信息→身份类型→工人  工
                if(StringUtils.isNotBlank(hrEmpBasic.getIdentityType())) {
                    String category1 = null;
                    switch (hrEmpBasic.getIdentityType()) {
                        // 身份类型 干部
                        case "1":
                            // 人员分类1 “干”对应的码值
                            category1 = "001";
                            break;

                        // 身份类型 工人
                        case "2":
                            // 人员分类1 “工”对应的码值
                            category1 = "002";
                            break;

                        default:
                    }
                    hrEmpBasic.setCategory1(category1);
                }
                // 同步人员分类4
                String category4 = null;
                hrEmpBasic.setCategory4(category4);
                // 同步人员分类7
                String category7 = null;
                // 获取专业职务任职资格列表
                List<HrEmpProfessionalQualification> qualificationList = hrEmpProfessionalQualificationService.selectMsg(hrEmpBasic.getUserCode());
                if(!qualificationList.isEmpty()) {
                    for(HrEmpProfessionalQualification qualification:qualificationList){
                        // 找到目前最高的任职资格，以此为判断标准
                        if(StringUtils.isNotBlank(qualification.getIsHighest())  && SystemConstants.TRUE_FLAG.equals(qualification.getIsHighest())) {
                            /*
                              00010	正高级 正
                              00020	高级 高
                              00030	中级 师
                              00040	助理级 助
                              00050	员级 员
                              00060	未聘任专业技术职务
                             */
                            switch(qualification.getQuaTitleLevel()) {
                                case "00010":
                                    category7 = "001";
                                    break;
                                case "00020":
                                    category7 = "002";
                                    break;
                                case "00030":
                                    category7 = "003";
                                    break;
                                case "00040":
                                    category7 = "004";
                                    break;
                                case "00050":
                                    category7 = "005";
                                    break;
                                default:
                            }
                        }
                    }
                }
                hrEmpBasic.setCategory7(category7);
                // 同步人员分类8
                String category8 = null;
                hrEmpBasic.setCategory8(category8);

                // 更新方法
                LambdaUpdateWrapper<HrEmpBasic> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(HrEmpBasic::getUserCode, hrEmpBasic.getUserCode())
                        .set(HrEmpBasic::getCategory1, hrEmpBasic.getCategory1())
                        .set(HrEmpBasic::getCategory4, hrEmpBasic.getCategory4())
                        .set(HrEmpBasic::getCategory7, hrEmpBasic.getCategory7())
                        .set(HrEmpBasic::getCategory8, hrEmpBasic.getCategory8());
                hrEmpBasicService.update(hrEmpBasic, wrapper);
            }
            return new ResponseResult<>(HttpStatus.OK.value(), "人员分类同步成功");
        } catch (Exception e) {
            log.error("同步失败，{}", e.getMessage());
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "人员分类同步失败" + e.getMessage());
        }
    }


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
            boolean flag = RoleUtils.check("人力资源部-招聘管理专员");
            if (flag) {
                log.info("权限校验通过，用户拥有'人力资源部-招聘管理专员'角色，直接进行更新");
                try{
                    updateHrEmpBasic(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'人力资源部-招聘管理专员'角色");

            // 1. 获取当前数据快照
            HrEmpBasicDTO originalData = hrEmpBasicService.selectMsg(changeReqVO.getUserCode()).get(0);
            if (originalData == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "员工信息不存在");
            }

            // 2. 创建变更申请文档
            HrEmpBasicChangeRequest changeRequest = new HrEmpBasicChangeRequest();
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
            String bizCode = "BASIC-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            // 封装数据主键，存在MongoDB里面的生成的simpleUUID
            instanceDTO.setBizId("basic_" + changeRequest.getId());
            // 封装业务模板编码
            instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);
            // 封装业务表单数据，20250924上线暂时没用，如果之后需要修改审批流分支，需使用该数据
            Map<String, Object> formData = new HashMap<>();
            formData.put("type","basic");
            formData.put("roleCode", "wmrz_audit");
            formData.put("Basic",MyUtils.convertToMapList(changeReqVO));
            instanceDTO.setFormData(formData);
            log.info("流程测试instanceDTO" + instanceDTO);
            // 返回流程实例id
            ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
            log.info("流程测试processInstanceVo" + processInstanceVo);

            // 5. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
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
        log.info("requestId:" + requestId);
        requestId = MyUtils.subPk(requestId);
        // 1. 从MongoDB获取变更请求
        HrEmpBasicChangeRequest request = mongoTemplate.findById(requestId, HrEmpBasicChangeRequest.class);
        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
        }

        // 2. 构建返回结果（直接返回两个列表）
        Map<String, Object> result = new HashMap<>();

        // 原始数据列表（转换为Map列表或直接使用对象）
        result.put("originalData", MyUtils.convertToMapList(request.getOriginalData()));

        // 审批流中的数据列表
        result.put("changedData", MyUtils.convertToMapList(request.getChangedData()));

        // 基础信息
        result.put("requestId", requestId);
        result.put("createTime", request.getCreateTime());

        return new ResponseResult<>(HttpStatus.OK.value(), "成功", result);
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工基本信息表实体")
    @AuditLog(value = "更新员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<String> updateHrEmpBasic(@Valid @RequestBody HrEmpBasicDTO updateReqVO) {
        log.info("进入更新调用成功");
        //基本信息中，健康状况，户口相关需要跨表保存
        try {
            //健康信息部分
            String userCode = updateReqVO.getUserCode();
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
            log.info("健康信息部分更新成功");
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
            log.info("户口信息部分更新成功");
            if (updateReqVO.getId() == null) {
                updateReqVO.setId(IdUtil.simpleUUID());
                hrEmpBasicService.save(updateReqVO);
            }
            // 更新证件信息
            HrEmpCertificate certificate = hrEmpCertificateService.selectByUserCode(userCode);
            if (certificate == null) {
                certificate = new HrEmpCertificate();
                certificate.setUserCode(userCode);
                certificate.setId(IdUtil.simpleUUID());
            }
            certificate.setCertiId(updateReqVO.getCertiId());
            certificate.setCertiType(updateReqVO.getCertiType());
            hrEmpCertificateService.saveOrUpdate(certificate);
            log.info("证件信息部分更新成功");
            // 更新通讯信息
            HrEmpCommunication communication = hrEmpCommunicationService.selectByUserCode(userCode);
            if (communication == null) {
                communication = new HrEmpCommunication();
                communication.setUserCode(userCode);
                communication.setId(IdUtil.simpleUUID());
            }
            communication.setMobilePhone(updateReqVO.getMobilePhone());
            communication.setPersonalEmail(updateReqVO.getPersonalEmail());
            communication.setAddress(updateReqVO.getAddress());
            communication.setWorkEmail(updateReqVO.getWorkEmail());
            hrEmpCommunicationService.saveOrUpdate(communication);
            log.info("通讯信息部分更新成功");
            // 更新基本信息
            if (updateReqVO.getId() == null) {
                updateReqVO.setId(IdUtil.simpleUUID());
                hrEmpBasicService.save(updateReqVO);
            }
            hrEmpBasicService.updateById(updateReqVO);
            log.info("其余基本信息部分更新成功");
        } catch (Exception e) {
            log.error("更新或保存时出错：\n" + "入参： " + updateReqVO + "\n" + "错误日志： " + e.getMessage() + "\n");
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "更新员工教育信息表实体失败");
        }
        log.info("准备返回成功");
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
