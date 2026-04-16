package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpForeignLanguage;
import com.crrc.entity.LoginUser;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mongodb.HrEmpForeignLanguageChangeRequest;
import com.crrc.mongodb.service.MongoDBService;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpForeignLanguageDTO;
import com.crrc.service.impl.HrEmpForeignLanguageServiceImpl;
import com.crrc.utils.*;
import com.crrc.utils.listener.ForeignLanguageImportListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 外语能力信息表 前端控制器
 * </p>
 *
 * @author system
 * @since 2026-04-16
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpForeignLanguage")
@RefreshScope
@Tag(name = "外语能力信息表", description = "外语能力信息表")
public class HrEmpForeignLanguageController {

    @Autowired
    private HrEmpForeignLanguageServiceImpl hrEmpForeignLanguageService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    private WkDataAPi wkDataAPi;
    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private DiffResolver diffResolver;

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工外语能力实体")
    @AuditLog(value = "获得员工外语能力实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpForeignLanguage>> getMsg(@RequestParam String userCode) {
        List<HrEmpForeignLanguage> result = hrEmpForeignLanguageService.selectMsg(userCode);

        try {
            // 处理附件信息
            hrEmpForeignLanguageService.resolveFiles(result);
        } catch (Exception e) {
            log.info("外语能力附件信息解析失败");
        }
        // 翻译字典码为显示值
        hrEmpForeignLanguageService.resolveLanguageAndCertValues(result);

        log.info("userCode:{},外语能力数据：{}", userCode, JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工外语能力实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取外语能力信息表列表 page")
    @AuditLog(value = "获取外语能力信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage> pageHrEmpForeignLanguage(@Validated @RequestBody(required = false) HrEmpForeignLanguage pageReqVO,
                                                          @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                          @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpForeignLanguage() : pageReqVO;
        Page<HrEmpForeignLanguage> page = new Page<>(pageNo, pageSize);
        hrEmpForeignLanguageService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取外语能力信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取外语能力信息表列表")
    @AuditLog(value = "获取外语能力信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpForeignLanguage>> queryHrEmpForeignLanguage(@Validated @RequestBody(required = false) HrEmpForeignLanguage queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpForeignLanguage() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取外语能力信息表列表成功", hrEmpForeignLanguageService.query(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得外语能力信息表实体")
    @AuditLog(value = "获得外语能力信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpForeignLanguage> getHrEmpForeignLanguage(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取外语能力信息表实体成功", hrEmpForeignLanguageService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工外语能力信息表实体")
    @AuditLog(value = "创建员工外语能力信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpForeignLanguage(@Valid @RequestBody HrEmpForeignLanguage createReqVO) {
        // 校验证书手写内容：仅当证书名称码值为"10"时允许填写
        hrEmpForeignLanguageService.clearCertificateNameWriteIfNotApplicable(Collections.singletonList(createReqVO));
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工外语能力信息表实体", hrEmpForeignLanguageService.save(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工外语能力信息表实体")
    @AuditLog(value = "更新员工外语能力信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpForeignLanguage(@Valid @RequestBody HrEmpForeignLanguageDTO updateReqVO) {
        try {
            // 校验证书手写内容：仅当证书名称码值为"10"时允许填写
            hrEmpForeignLanguageService.clearCertificateNameWriteIfNotApplicable(updateReqVO.getList());

            // 查询现有数据
            List<HrEmpForeignLanguage> existList = hrEmpForeignLanguageService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpForeignLanguage::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpForeignLanguage> toSaveOrUpdate = new ArrayList<>();

            for (HrEmpForeignLanguage language : updateReqVO.getList()) {
                if (language.getId() == null) {
                    // 生成新ID
                    language.setId(IdUtil.simpleUUID());
                }
                language.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(language);
                // 移除不需要删除的ID
                existIds.remove(language.getId());
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpForeignLanguageService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpForeignLanguageService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工外语能力信息表实体");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工外语能力信息表实体");
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除外语能力信息表实体")
    @AuditLog(value = "删除外语能力信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpForeignLanguage(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除外语能力信息表实体成功", hrEmpForeignLanguageService.removeById(id));
    }

    @PostMapping("/export/template")
    @AuditLog(value = "导出外语能力信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出外语能力信息表报表")
    public void exportHrEmpForeignLanguage(@RequestParam(required = false, defaultValue = "xlsx") String type,
                                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("外语能力信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        List<HrEmpForeignLanguage> emptyList = new ArrayList<>();
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpForeignLanguage.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("外语能力信息表报表")
                .doWrite(emptyList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入外语能力信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入外语能力信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcelFactory.read(file.getInputStream(), HrEmpForeignLanguage.class,
                        new ImportExcelListener<HrEmpForeignLanguage, HrEmpForeignLanguageServiceImpl>(hrEmpForeignLanguageService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入外语能力信息表报表成功");

    }

    @PostMapping("/importForeignLanguage")
    public ResponseResult<String> importForeignLanguage(@RequestBody MultipartFile file) {
        try {
            EasyExcelFactory.read(file.getInputStream(), HrEmpForeignLanguage.class,
                            new ForeignLanguageImportListener(hrEmpForeignLanguageService))
                    .headRowNumber(1)
                    .sheet()
                    .doRead();
            return new ResponseResult<>(HttpStatus.OK.value(), "导入外语能力信息表报表成功");
        } catch (Exception e) {
            log.error("导入外语能力信息表报表错误", e);
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/tempSave")
    @Operation(summary = "暂存员工信息")
    public ResponseResult<String> tempSave(@RequestBody HrEmpForeignLanguageDTO saveDTO) {
        mongoDBService.saveDataByUserCode(saveDTO, saveDTO.getUserCode(), "hr_emp_foreign_language_temp_save_data");
        return ResponseResult.success("OK");
    }

    @PostMapping("/queryData/{type}")
    @Operation(summary = "查询驳回/暂存的数据")
    public ResponseResult<HrEmpForeignLanguageDTO> tempQuery(@RequestBody HrEmpForeignLanguageDTO queryDTO, @PathVariable String type) {
        if ("tempSave".equals(type)) {
            return ResponseResult.success(
                    mongoDBService.getById(queryDTO.getUserCode(), HrEmpForeignLanguageDTO.class, "hr_emp_foreign_language_temp_save_data")
            );
        }

        return ResponseResult.success(mongoDBService.getById(queryDTO.getUserCode(), HrEmpForeignLanguageDTO.class, "hr_emp_foreign_language_submit_data"));
    }

    @PostMapping("/submit-change")
    @Operation(summary = "提交员工外语能力变更申请")
    public ResponseResult<List<String>> submitChange(@Valid @RequestBody HrEmpForeignLanguageDTO changeReqVO) {
        try {
            // 校验证书手写内容：仅当证书名称码值为"10"时允许填写
            hrEmpForeignLanguageService.clearCertificateNameWriteIfNotApplicable(changeReqVO.getList());

            // ================== 超级管理员直接更新 ==================
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try {
                    updateHrEmpForeignLanguage(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!", new ArrayList<>());
                } catch (Exception e) {
                    log.info("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }

            log.info("权限校验失败，用户缺少'公司超级管理员'角色");

            // ================== 获取登录人信息 ==================
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            if (loginUser == null || loginUser.getUser() == null) {
                throw new IllegalArgumentException("获取登录用户信息失败，用户为空");
            }

            if (StringUtils.isBlank(loginUser.getUser().getJobNo())) {
                throw new IllegalArgumentException("获取用户工号失败，工号为空");
            }

            // ================== 查询部门 ==================
            LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpBasic::getUserCode, loginUser.getUser().getJobNo());
            HrEmpBasic emp = hrEmpBasicMapper.selectOne(wrapper);

            if (emp == null || StringUtils.isBlank(emp.getDept())) {
                throw new IllegalArgumentException("获取用户部门失败");
            }

            String deptCode = emp.getDept();

            // ================== 获取组织树（只查一次，优化性能）==================
            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
            if (listResponseResultDTO == null ||
                    org.apache.commons.lang3.ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                throw new IllegalArgumentException("获取组织树失败");
            }

            List<OrganizationTreeDTO> orgTree = listResponseResultDTO.getData();
            String parentCode = findParentCode(orgTree, deptCode);

            // ================== 查询原始数据（只查一次）==================
            List<HrEmpForeignLanguage> originalData = getMsg(changeReqVO.getUserCode()).getData();

            // 检查是否存在正在修改中的数据
            for (HrEmpForeignLanguage item : originalData) {
                if ("1".equals(item.getIsUpdating())) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // 如果没有数据，需要校验mongoDB中是否存在已有数据
            if (ObjectUtils.isEmpty(originalData)) {
                List<HrEmpForeignLanguageChangeRequest> reqs = mongoDBService.getByUserCode(changeReqVO.getUserCode(), HrEmpForeignLanguageChangeRequest.class, "hr_emp_foreign_language_change_requests");
                if (reqs.stream().anyMatch(e -> "PENDING".equals(e.getStatus()))) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // ================== 使用diffResolver扫描变更字段 ==================
            diffResolver.resolveDifference(originalData, changeReqVO.getList(), HrEmpForeignLanguage.class);

            // 保存到MongoDB之后再保存暂存数据
            mongoDBService.saveDataByUserCode(changeReqVO, changeReqVO.getUserCode(), "hr_emp_foreign_language_submit_data");

            // ================== 创建变更申请（单一审批流，不区分归口单位）==================
            List<String> requestIds = new ArrayList<>();

            try {
                HrEmpForeignLanguageChangeRequest changeRequest = new HrEmpForeignLanguageChangeRequest();
                String id = IdUtil.simpleUUID();
                changeRequest.setId(id);
                changeRequest.setUserCode(changeReqVO.getUserCode());

                // originalData放入全部原数据
                changeRequest.setOriginalData(originalData);

                // changedData放入所有有变更的数据
                List<HrEmpForeignLanguage> changedDataList = changeReqVO.getList().stream()
                        .filter(e -> e.getMarkList() != null && !e.getMarkList().isEmpty())
                        .collect(Collectors.toList());
                changeRequest.setChangedData(changedDataList);

                changeRequest.setStatus("PENDING");
                mongoTemplate.save(changeRequest);

                // 启动流程
                StartProcessInstanceDTO instanceDTO = createProcessInstanceDTO(id, deptCode, parentCode);

                ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);

                // 回写流程ID
                changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
                mongoTemplate.save(changeRequest);

                requestIds.add(changeRequest.getId());

                log.info("审批流已启动，请求ID: {}", id);

            } catch (Exception e) {
                log.error("审批流提交失败: {}", e.getMessage(), e);
            }

            if (requestIds.isEmpty()) {
                return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "全部提交失败");
            }

            return new ResponseResult<>(HttpStatus.OK.value(), "变更申请已提交", requestIds);

        } catch (Exception e) {
            log.error("提交变更申请失败: {}", e.getMessage(), e);
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    private String findParentCode(List<OrganizationTreeDTO> orgTree, String deptCode) {
        String parentCode = null;
        for (OrganizationTreeDTO root : orgTree) {
            if (TreeUtils.containsBfs(root,
                    n -> StringUtils.equals(deptCode, n.getOrgCode()),
                    OrganizationTreeDTO::getChildren)) {
                parentCode = root.getOrgCode();
                break;
            }
        }
        return parentCode;
    }

    private StartProcessInstanceDTO createProcessInstanceDTO(String id, String deptCode, String parentCode) {
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        String bizCode = "LANGUAGE-" + System.currentTimeMillis();
        instanceDTO.setBizCode(bizCode);
        instanceDTO.setBizId("language_" + id);
        instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);

        Map<String, Object> formData = new HashMap<>();
        formData.put("deptCode", deptCode);
        formData.put("type", "language");
        formData.put("companyCode", parentCode);

        if (StringUtils.isNotBlank(parentCode) && parentCode.equals("0132")) {
            formData.put("roleCode", "hrsc_dept_emp_info_mgr");
        } else {
            formData.put("roleCode", "hrsc_sub_emp_info_mgr");
        }

        instanceDTO.setFormData(formData);
        log.info("启动流程参数: {}", JSON.toJSONString(instanceDTO));
        return instanceDTO;
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        try {
            log.info("外语能力变更请求ID: {}", requestId);
            requestId = MyUtils.subPk(requestId);
            // 1. 从MongoDB获取变更请求
            HrEmpForeignLanguageChangeRequest request = mongoTemplate.findById(requestId, HrEmpForeignLanguageChangeRequest.class);
            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
            }

            // 由于附件信息不存在MongoDB中，先尝试进行查询并赋值
            List<HrEmpForeignLanguage> originalData = request.getOriginalData();
            hrEmpForeignLanguageService.resolveFiles(originalData);
            List<HrEmpForeignLanguage> changedData = request.getChangedData();
            hrEmpForeignLanguageService.resolveFiles(changedData);

            // 2. 构建返回结果（直接返回两个列表）
            Map<String, Object> result = new HashMap<>();

            // 原始数据列表
            result.put("originalData", MyUtils.convertToMapList(request.getOriginalData()));

            // 审批流中的数据列表
            result.put("changedData", MyUtils.convertToMapList(request.getChangedData()));

            // 基础信息
            result.put("requestId", requestId);
            result.put("createTime", request.getCreateTime());

            return new ResponseResult<>(HttpStatus.OK.value(), "成功", result);
        } catch (Exception e) {
            log.error("获取外语能力变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }
}
