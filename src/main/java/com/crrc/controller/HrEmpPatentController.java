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
import com.crrc.entity.HrEmpPaper;
import com.crrc.entity.HrEmpPatent;
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
import com.crrc.mongodb.HrEmpPaperChangeRequest;
import com.crrc.mongodb.HrEmpPatentChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpPaperDTO;
import com.crrc.pojo.dto.HrEmpPatentDTO;
import com.crrc.service.impl.HrEmpPatentServiceImpl;
import com.crrc.utils.*;
import com.crrc.utils.listener.PatentImportListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
 * 专利信息表 前端控制器
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:36:49
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpPatent")
@RefreshScope
@Tag(name = "专利信息表", description = "专利信息表")
public class HrEmpPatentController {

    @Autowired
    private HrEmpPatentServiceImpl hrEmpPatentService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    private WkDataAPi wkDataAPi;

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工专利信息实体")
    @AuditLog(value = "获得员工专利信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpPatent>> getMsg(@RequestParam String userCode) {
        List<HrEmpPatent> result = hrEmpPatentService.selectMsg(userCode);
        try {
            // 处理附件信息
            hrEmpPatentService.resolveFiles(result);
        } catch (Exception e) {
            log.warn("专利附件信息解析失败");
        }
        log.info("userCode:{},专利数据：{}", userCode, JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工专利信息实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取专利信息表列表 page")
    @AuditLog(value = "获取专利信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage> pageHrEmpPatent(@Validated @RequestBody(required = false) HrEmpPatent pageReqVO,
                                                 @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                 @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpPatent() : pageReqVO;
        Page<HrEmpPatent> page = new Page<>(pageNo, pageSize);
        hrEmpPatentService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取专利信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取专利信息表列表")
    @AuditLog(value = "获取专利信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpPatent>> queryHrEmpPatent(@Validated @RequestBody(required = false) HrEmpPatent queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpPatent() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取专利信息表列表成功", hrEmpPatentService.query(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得专利信息表实体")
    @AuditLog(value = "获得专利信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpPatent> getHrEmpPatent(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取专利信息表实体成功", hrEmpPatentService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工专利信息表实体")
    @AuditLog(value = "创建员工专利信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpPatent(@Valid @RequestBody HrEmpPatent createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工专利信息表实体", hrEmpPatentService.save(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工专利信息表实体")
    @AuditLog(value = "更新员工专利信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpPatent(@Valid @RequestBody HrEmpPatentDTO updateReqVO) {
        try {
            // 查询现有数据
            List<HrEmpPatent> existList = hrEmpPatentService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpPatent::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpPatent> toSaveOrUpdate = new ArrayList<>();

            for (HrEmpPatent patent : updateReqVO.getList()) {
                if (patent.getId() == null) {
                    // 生成新ID
                    patent.setId(IdUtil.simpleUUID());
                }
                patent.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(patent);
                // 移除不需要删除的ID
                existIds.remove(patent.getId());
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpPatentService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpPatentService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工专利信息表实体");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工专利信息表实体");
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除专利信息表实体")
    @AuditLog(value = "删除专利信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpPatent(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除专利信息表实体成功", hrEmpPatentService.removeById(id));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出专利信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出专利信息表报表")
    public void exportHrEmpPatent(@Validated @RequestBody(required = false) HrEmpPatent exportReqVO,
                                  @RequestParam(required = false, defaultValue = "xlsx") String type,
                                  HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ? "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("专利信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + type);

        exportReqVO = (exportReqVO == null) ? new HrEmpPatent() : exportReqVO;
        List<HrEmpPatent> hrEmpPatentList = hrEmpPatentService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ? ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpPatent.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("专利信息表报表")
                .doWrite(hrEmpPatentList);
    }

    @PostMapping("/importPatent")
    @AuditLog(value = "导入专利信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入专利信息表报表")
    public ResponseResult<Boolean> importPatent(@RequestBody MultipartFile file) throws IOException {
        EasyExcelFactory.read(file.getInputStream(), HrEmpPatent.class,
                        new PatentImportListener(hrEmpPatentService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入专利信息表报表成功");
    }

    @PostMapping("/import")
    @AuditLog(value = "导入专利信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入专利信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpPatent.class,
                        new ImportExcelListener<HrEmpPatent, HrEmpPatentServiceImpl>(hrEmpPatentService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入专利信息表报表成功");
    }

    @PostMapping("/submit-change")
    @Operation(summary = "提交员工专利变更申请")
    public ResponseResult<List<String>> submitChange(@Valid @RequestBody HrEmpPatentDTO changeReqVO) {
        try {
            // ================== 超级管理员直接更新 ==================
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try {
                    updateHrEmpPatent(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e) {
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }

            log.warn("权限校验失败，用户缺少'公司超级管理员'角色");

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

            // ================== 获取组织树（只查一次，优化性能） ==================
            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
            if (listResponseResultDTO == null || org.apache.commons.lang3.ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                throw new IllegalArgumentException("获取组织树失败");
            }

            List<OrganizationTreeDTO> orgTree = listResponseResultDTO.getData();
            String parentCode = findParentCode(orgTree, deptCode);

            // ================== 查询原始数据（只查一次） ==================
            List<HrEmpPatent> originalData = getMsg(changeReqVO.getUserCode()).getData();

            // 构建Id和List序号的Map，用于简化查询原数据的过程
            Map<String, Integer> idmap = buildIdMap(originalData);

            // ================== 结果ID集合 ==================
            List<String> requestIds = new ArrayList<>();

            // ================== 核心：逐条处理 ==================
            for (HrEmpPatent entity : changeReqVO.getList()) {
                try {
                    // 创建变更申请
                    HrEmpPatentChangeRequest changeRequest = new HrEmpPatentChangeRequest();
                    changeRequest.setId(IdUtil.simpleUUID());
                    changeRequest.setUserCode(changeReqVO.getUserCode());

                    if (StringUtils.isBlank(entity.getId())) {
                        changeRequest.setOriginalData(null);
                    } else if (entity.getId().contains("NOTCHANGE")) {
                        String cleanedId = entity.getId().replace("NOTCHANGE", "").trim();
                        int num = idmap.get(cleanedId);
                        originalData.remove(num);
                        continue;
                    } else {
                        int num = idmap.get(entity.getId());
                        List<HrEmpPatent> origanalList = new ArrayList<>();
                        origanalList.add(originalData.get(num));
                        changeRequest.setOriginalData(origanalList);
                        originalData.remove(num);
                    }

                    changeRequest.setChangedData(Collections.singletonList(entity));
                    changeRequest.setStatus("PENDING");
                    mongoTemplate.save(changeRequest);

                    // 启动流程
                    StartProcessInstanceDTO instanceDTO = createProcessInstanceDTO(deptCode, parentCode, entity);
                    ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);

                    // 回写流程ID
                    changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
                    mongoTemplate.save(changeRequest);

                    requestIds.add(changeRequest.getId());
                } catch (Exception e) {
                    log.error("单条审批提交失败: {}", JSON.toJSONString(entity), e);
                }
            }

            // 处理删除数据
            for (HrEmpPatent entity : originalData) {
                try {
                    HrEmpPatentChangeRequest changeRequest = new HrEmpPatentChangeRequest();
                    changeRequest.setId(IdUtil.simpleUUID());
                    changeRequest.setUserCode(changeReqVO.getUserCode());

                    List<HrEmpPatent> origanalList = new ArrayList<>();
                    origanalList.add(entity);
                    changeRequest.setOriginalData(origanalList);

                    List<HrEmpPatent> changedList = new ArrayList<>();
                    HrEmpPatent changed = new HrEmpPatent();
                    changed.setId(entity.getId());
                    changedList.add(changed);
                    changeRequest.setChangedData(changedList);

                    changeRequest.setStatus("PENDING");
                    mongoTemplate.save(changeRequest);

                    StartProcessInstanceDTO instanceDTO = createProcessInstanceDTO(deptCode, parentCode, entity);
                    ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);

                    changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
                    mongoTemplate.save(changeRequest);

                    requestIds.add(changeRequest.getId());
                } catch (Exception e) {
                    log.error("单条审批提交失败: {}", JSON.toJSONString(entity), e);
                }
            }

            if (requestIds.isEmpty()) {
                return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "全部提交失败");
            }

            return new ResponseResult<>(HttpStatus.OK.value(), "变更申请已提交", requestIds);
        } catch (Exception e) {
            log.error("提交变更申请失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
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

    private Map<String, Integer> buildIdMap(List<HrEmpPatent> originalData) {
        Map<String, Integer> idmap = new HashMap<>();
        Integer i = 0;
        for (HrEmpPatent empPatent : originalData) {
            idmap.put(empPatent.getId(), i);
            i++;
        }
        return idmap;
    }

    private StartProcessInstanceDTO createProcessInstanceDTO(String deptCode, String parentCode, HrEmpPatent entity) {
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        String bizCode = "PATENT-" + System.currentTimeMillis();
        instanceDTO.setBizCode(bizCode);
        instanceDTO.setBizId("patent_" + IdUtil.simpleUUID());
        instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);

        Map<String, Object> formData = new HashMap<>();
        formData.put("deptCode", deptCode);
        formData.put("type", "patent");
        formData.put("companyCode", parentCode);

        if (StringUtils.isNotBlank(parentCode) && parentCode.equals("0132")) {
            formData.put("roleCode", "hrsc_dept_emp_info_mgr");
        } else {
            formData.put("roleCode", "hrsc_sub_emp_info_mgr");
        }

        formData.put("Patent", Collections.singletonList(
                JSON.parseObject(JSON.toJSONString(entity), Map.class)
        ));

        instanceDTO.setFormData(formData);
        log.info("启动流程参数: {}", JSON.toJSONString(instanceDTO));
        return instanceDTO;
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        try {
            log.info("专利变更请求ID: {}", requestId);
            requestId = MyUtils.subPk(requestId);
            // 1. 从MongoDB获取变更请求
            HrEmpPatentChangeRequest request = mongoTemplate.findById(requestId, HrEmpPatentChangeRequest.class);
            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
            }

            // 由于附件信息不存在MongoDB中，先尝试进行查询并赋值
            List<HrEmpPatent> originalData = request.getOriginalData();
            hrEmpPatentService.resolveFiles(originalData);
            List<HrEmpPatent> changedData = request.getChangedData();
            hrEmpPatentService.resolveFiles(changedData);

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
            log.error("获取专利变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }
}
