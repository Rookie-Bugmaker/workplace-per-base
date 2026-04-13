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
import com.crrc.entity.HrEmpHonor;
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
import com.crrc.mongodb.HrEmpHonorChangeRequest;
import com.crrc.mongodb.service.MongoDBService;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpHonorDTO;
import com.crrc.service.impl.HrEmpHonorServiceImpl;
import com.crrc.utils.*;
import com.crrc.utils.listener.HonorImportListener;
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
 * 荣誉信息表 前端控制器
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:38:02
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpHonor")
@RefreshScope
@Tag(name = "荣誉信息表", description = "荣誉信息表")
public class HrEmpHonorController {

    @Autowired
    private HrEmpHonorServiceImpl hrEmpHonorService;

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
    @Operation(summary = "获得员工荣誉信息实体")
    @AuditLog(value = "获得员工荣誉信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpHonor>> getMsg(@RequestParam String userCode) {
        List<HrEmpHonor> result = hrEmpHonorService.selectMsg(userCode);

        try {
            // 处理附件信息
            hrEmpHonorService.resolveFiles(result);
        } catch (Exception e) {
            log.info("荣誉附件信息解析失败");
        }
        log.info("userCode:{},荣誉数据：{}", userCode, JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工荣誉信息实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取荣誉信息表列表 page")
    @AuditLog(value = "获取荣誉信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage> pageHrEmpHonor(@Validated @RequestBody(required = false) HrEmpHonor pageReqVO,
                                                @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpHonor() : pageReqVO;
        Page<HrEmpHonor> page = new Page<>(pageNo, pageSize);
        hrEmpHonorService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取荣誉信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取荣誉信息表列表")
    @AuditLog(value = "获取荣誉信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpHonor>> queryHrEmpHonor(@Validated @RequestBody(required = false) HrEmpHonor queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpHonor() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取荣誉信息表列表成功", hrEmpHonorService.query(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得荣誉信息表实体")
    @AuditLog(value = "获得荣誉信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpHonor> getHrEmpHonor(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取荣誉信息表实体成功", hrEmpHonorService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工荣誉信息表实体")
    @AuditLog(value = "创建员工荣誉信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpHonor(@Valid @RequestBody HrEmpHonor createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工荣誉信息表实体", hrEmpHonorService.save(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工荣誉信息表实体")
    @AuditLog(value = "更新员工荣誉信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpHonor(@Valid @RequestBody HrEmpHonorDTO updateReqVO) {
        try {
            // 查询现有数据
            List<HrEmpHonor> existList = hrEmpHonorService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpHonor::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpHonor> toSaveOrUpdate = new ArrayList<>();

            for (HrEmpHonor honor : updateReqVO.getList()) {
                if (honor.getId() == null) {
                    // 生成新ID
                    honor.setId(IdUtil.simpleUUID());
                }
                honor.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(honor);
                // 移除不需要删除的ID
                existIds.remove(honor.getId());
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpHonorService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpHonorService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工荣誉信息表实体");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工荣誉信息表实体");
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除荣誉信息表实体")
    @AuditLog(value = "删除荣誉信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpHonor(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除荣誉信息表实体成功", hrEmpHonorService.removeById(id));
    }

    @PostMapping("/export/template")
    @AuditLog(value = "导出荣誉信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出荣誉信息表报表")
    public void exportHrEmpHonor(@RequestParam(required = false, defaultValue = "xlsx") String type,
                                 HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("荣誉信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        List<HrEmpHonor> emptyList = new ArrayList<>();
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpHonor.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("荣誉信息表报表")
                .doWrite(emptyList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入荣誉信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入荣誉信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcelFactory.read(file.getInputStream(), HrEmpHonor.class,
                        new ImportExcelListener<HrEmpHonor, HrEmpHonorServiceImpl>(hrEmpHonorService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入荣誉信息表报表成功");

    }

    @PostMapping("/importHonor")
    public ResponseResult<String> importHonor(@RequestBody MultipartFile file) {
        try {
            EasyExcelFactory.read(file.getInputStream(), HrEmpHonor.class,
                            new HonorImportListener(hrEmpHonorService))
                    .headRowNumber(1)
                    .sheet()
                    .doRead();
            return new ResponseResult<>(HttpStatus.OK.value(), "导入荣誉信息表报表成功");
        } catch (Exception e) {
            log.error("导入荣誉信息表报表错误", e);
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/tempSave")
    @Operation(summary = "暂存员工信息")
    public ResponseResult<String> tempSave(@RequestBody HrEmpHonorDTO saveDTO) {
        mongoDBService.saveDataByUserCode(saveDTO, saveDTO.getUserCode(), "hr_emp_honor_temp_save_data");
        return ResponseResult.success("OK");
    }

    @PostMapping("/queryData/{type}")
    @Operation(summary = "查询驳回/暂存的数据")
    public ResponseResult<HrEmpHonorDTO> tempQuery(@RequestBody HrEmpHonorDTO queryDTO, @PathVariable String type) {
        if("tempSave".equals(type)) {
            return ResponseResult.success(
                    mongoDBService.getById(queryDTO.getUserCode(), HrEmpHonorDTO.class, "hr_emp_honor_temp_save_data")
            );
        }

        return ResponseResult.success(mongoDBService.getById(queryDTO.getUserCode(), HrEmpHonorDTO.class, "hr_emp_honor_submit_data"));
    }

    @PostMapping("/submit-change")
    @Operation(summary = "提交员工荣誉变更申请")
    public ResponseResult<List<String>> submitChange(@Valid @RequestBody HrEmpHonorDTO changeReqVO) {
        // 直接保存暂存数据
        mongoDBService.saveDataByUserCode(changeReqVO, changeReqVO.getUserCode(), "hr_emp_honor_submit_data");

        try {
            // ================== 超级管理员直接更新 ==================
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try {
                    updateHrEmpHonor(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
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
            List<HrEmpHonor> originalData = getMsg(changeReqVO.getUserCode()).getData();

            // 检查是否存在正在修改中的数据
            for (HrEmpHonor item : originalData) {
                if ("1".equals(item.getIsUpdating())) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // 如果没有数据，需要校验mongoDB中是否存在已有数据
            if (ObjectUtils.isEmpty(originalData)) {
                List<HrEmpHonorChangeRequest> reqs = mongoDBService.getByUserCode(changeReqVO.getUserCode(), HrEmpHonorChangeRequest.class, "hr_emp_honor_change_requests");
                if (reqs.stream().anyMatch(e -> "PENDING".equals(e.getStatus()))) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // ================== 使用diffResolver扫描变更字段 ==================
            diffResolver.resolveDifference(originalData, changeReqVO.getList(), HrEmpHonor.class);

            // ================== 按归口单位分组处理数据 ==================
            Map<String, List<HrEmpHonor>> responsibleUnitGroup = new HashMap<>();

            // 处理新增和修改的数据：以新的归口单位为准
            for (HrEmpHonor entity : changeReqVO.getList()) {
                // 跳过无变化的数据（markList为空）
                if (entity.getMarkList() == null || entity.getMarkList().isEmpty()) {
                    continue;
                }

                // 校验归口单位（新增和修改以新的归口单位为准）
                String responsibleUnit = entity.getResponsibleUnit();
                if (responsibleUnit == null || responsibleUnit.trim().isEmpty()) {
                    throw new IllegalArgumentException("归口单位不能为空");
                }

                // 按新的归口单位分组
                responsibleUnitGroup.computeIfAbsent(responsibleUnit, k -> new ArrayList<>()).add(entity);
            }

//            // 2. 处理删除的数据：以原归口单位为准
//            // diffResolver会将删除的数据添加到changedData中，并标记$DELETE$
//            for (HrEmpHonor entity : changeReqVO.getList()) {
//                if (entity.getMarkList() != null && entity.getMarkList().contains("$DELETE$")) {
//                    // 从原数据中找到对应的记录获取归口单位
//                    String originalId = entity.getId();
//                    HrEmpHonor originalRecord = originalData.stream()
//                            .filter(o -> originalId.equals(o.getId()))
//                            .findFirst()
//                            .orElse(null);
//
//                    if (originalRecord == null) {
//                        log.info("未找到删除数据的原始记录，ID: {}", originalId);
//                        continue;
//                    }
//
//                    String responsibleUnit = originalRecord.getResponsibleUnit();
//                    if (responsibleUnit == null || responsibleUnit.trim().isEmpty()) {
//                        throw new IllegalArgumentException("原数据归口单位不能为空，数据ID: " + originalId);
//                    }
//
//                    // 创建删除标记的数据（只保留ID和markList）
//                    HrEmpHonor deletedEntity = new HrEmpHonor();
//                    deletedEntity.setId(originalId);
//                    deletedEntity.setResponsibleUnit(responsibleUnit);
//                    deletedEntity.setMarkList(entity.getMarkList());
//
//                    // 将删除数据加入对应归口单位组
//                    responsibleUnitGroup.computeIfAbsent(responsibleUnit, k -> new ArrayList<>()).add(deletedEntity);
//                }
//            }

            // ================== 为每个归口单位创建审批流 ==================
            List<String> requestIds = new ArrayList<>();

            for (Map.Entry<String, List<HrEmpHonor>> entry : responsibleUnitGroup.entrySet()) {
                String responsibleUnit = entry.getKey();
                List<HrEmpHonor> changedDataList = entry.getValue();

                try {
                    // 创建变更申请
                    HrEmpHonorChangeRequest changeRequest = new HrEmpHonorChangeRequest();
                    String id = IdUtil.simpleUUID();
                    changeRequest.setId(id);
                    changeRequest.setUserCode(changeReqVO.getUserCode());

                    // originalData放入全部原数据
                    changeRequest.setOriginalData(originalData);

                    // changedData根据归口单位严格筛选
                    changeRequest.setChangedData(changedDataList);

                    changeRequest.setStatus("PENDING");
                    mongoTemplate.save(changeRequest);

                    // 启动流程（取第一条数据用于流程参数）
                    HrEmpHonor firstEntity = changedDataList.get(0);
                    StartProcessInstanceDTO instanceDTO =
                            createProcessInstanceDTO(responsibleUnit, id, deptCode, parentCode, firstEntity);

                    ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);

                    // 回写流程ID
                    changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
                    mongoTemplate.save(changeRequest);

                    requestIds.add(changeRequest.getId());

                    log.info("归口单位[{}]的审批流已启动，请求ID: {}", responsibleUnit, id);

                } catch (Exception e) {
                    log.error("归口单位[{}]的审批流提交失败: {}", responsibleUnit, e.getMessage(), e);
                }
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

    private StartProcessInstanceDTO createProcessInstanceDTO(String responsibleUnit, String id,
                                                             String deptCode, String parentCode, HrEmpHonor entity) {
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        String bizCode = "HONOR-" + System.currentTimeMillis();
        instanceDTO.setBizCode(bizCode);
        instanceDTO.setBizId("honor_" + id);
        instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);

        Map<String, Object> formData = new HashMap<>();
        formData.put("responsibleUnit", responsibleUnit);
        formData.put("deptCode", deptCode);
        formData.put("type", "honor");
        formData.put("companyCode", parentCode);

        if (StringUtils.isNotBlank(parentCode) && parentCode.equals("0132")) {
            formData.put("roleCode", "hrsc_dept_emp_info_mgr");
        } else {
            formData.put("roleCode", "hrsc_sub_emp_info_mgr");
        }

        formData.put("Honor", Collections.singletonList(
                JSON.parseObject(JSON.toJSONString(entity), Map.class)
        ));

        instanceDTO.setFormData(formData);
        log.info("启动流程参数: {}", JSON.toJSONString(instanceDTO));
        return instanceDTO;
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        try {
            log.info("荣誉变更请求ID: {}", requestId);
            requestId = MyUtils.subPk(requestId);
            // 1. 从MongoDB获取变更请求
            HrEmpHonorChangeRequest request = mongoTemplate.findById(requestId, HrEmpHonorChangeRequest.class);
            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
            }

            // 由于附件信息不存在MongoDB中，先尝试进行查询并赋值
            List<HrEmpHonor> originalData = request.getOriginalData();
            hrEmpHonorService.resolveFiles(originalData);
            List<HrEmpHonor> changedData = request.getChangedData();
            hrEmpHonorService.resolveFiles(changedData);

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
        } catch (Exception e) {
            log.error("获取荣誉变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }
}
