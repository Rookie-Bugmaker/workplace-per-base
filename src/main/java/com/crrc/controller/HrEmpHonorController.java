package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
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
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpHonorDTO;
import com.crrc.service.impl.HrEmpHonorServiceImpl;
import com.crrc.utils.*;
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

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工荣誉信息实体")
    @AuditLog(value = "获得员工荣誉信息实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpHonor>> getMsg(@RequestParam String userCode) {
        List<HrEmpHonor> result = hrEmpHonorService.selectMsg(userCode);

        try{
            // 处理附件信息
            hrEmpHonorService.resolveFiles(result);
        } catch (Exception e) {
            log.warn("荣誉附件信息解析失败");
        }
        log.info("userCode:{},荣誉数据：{}",userCode, JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工荣誉信息实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取荣誉信息表列表 page")
    @AuditLog(value = "获取荣誉信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpHonor(@Validated @RequestBody(required = false) HrEmpHonor pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpHonor() : pageReqVO;
        Page<HrEmpHonor> page = new Page<>(pageNo, pageSize);
        hrEmpHonorService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取荣誉信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取荣誉信息表列表")
    @AuditLog(value = "获取荣誉信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
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
    
    @PostMapping("/export")
    @AuditLog(value = "导出荣誉信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出荣誉信息表报表")
    public void exportHrEmpHonor(@Validated @RequestBody(required = false) HrEmpHonor exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("荣誉信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpHonor() : exportReqVO;
        List<HrEmpHonor> hrEmpHonorList = hrEmpHonorService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpHonor.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("荣誉信息表报表")
                .doWrite(hrEmpHonorList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入荣誉信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入荣誉信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpHonor.class,
                new ImportExcelListener<HrEmpHonor, HrEmpHonorServiceImpl>(hrEmpHonorService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入荣誉信息表报表成功");

    }
    @PostMapping("/submit-change")
    @Operation(summary = "提交员工荣誉变更申请")
    public ResponseResult<String> submitChange(@Valid @RequestBody  HrEmpHonorDTO changeReqVO) {
        try {
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try{
                    updateHrEmpHonor(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'公司超级管理员'角色");
            // 1. 获取当前数据快照
            List<HrEmpHonor> originalData = getMsg(changeReqVO.getUserCode()).getData();
            log.info("提交审批流时查询回来的教育经历原始数据：{}", JSON.toJSONString(originalData, SerializerFeature.PrettyFormat));

            // 2. 创建变更申请文档
            HrEmpHonorChangeRequest changeRequest = new HrEmpHonorChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);
            changeRequest.setChangedData(changeReqVO.getList());
            changeRequest.setStatus("PENDING");

            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);

            // 4. 启动审批流程
            // 封装数据，提交审批流
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "HONOR-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            // 封装数据主键，存在MongoDB里面的生成的simpleUUID
            instanceDTO.setBizId("honor_" +changeRequest.getId());
            // 封装业务模板编码
            instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);
            // 封装业务表单数据，20250924上线暂时没用，如果之后需要修改审批流分支，需使用该数据
            Map<String, Object> formData = new HashMap<>();
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            if(loginUser == null || loginUser.getUser() == null) {
                log.error("获取登录用户信息失败，用户为空");
                throw new IllegalArgumentException("获取登录用户信息失败，用户为空");
            }

            if(StringUtils.isBlank(loginUser.getUser().getJobNo())) {
                log.error("获取用户工号失败，工号为空");
                throw new IllegalArgumentException("获取用户工号失败，工号为空");
            }

            LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpBasic::getUserCode, loginUser.getUser().getJobNo());
            String deptCode = hrEmpBasicMapper.selectOne(wrapper).getDept();

            if(StringUtils.isBlank(deptCode)) {
                log.error("获取用户部门失败，部门查询错误，工号:" + loginUser.getUser().getJobNo());
                throw new IllegalArgumentException("获取用户部门失败，部门查询错误");
            }

            formData.put("deptCode", deptCode);
            formData.put("type","edu");

            // 如果是子公司的，则PUT子公司管理员，反之PUT部门管理员
            // 获取全部部门信息
            // 获取所有的组织数据
            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
            if (listResponseResultDTO == null || org.apache.commons.lang3.ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                throw new IllegalArgumentException("外部接口错误，获取树形组织信息失败");
            }
            List<OrganizationTreeDTO> data = listResponseResultDTO.getData();
            String parentCode = null;
            for(OrganizationTreeDTO root : data) {
                if (TreeUtils.containsBfs(root, n -> StringUtils.equals(deptCode, n.getOrgCode()), OrganizationTreeDTO::getChildren)) {
                    parentCode = root.getOrgCode();
                    break;
                }
            }
            formData.put("companyCode", parentCode);
            if(StringUtils.isNotBlank(parentCode) && parentCode.equals("0132")) {
                formData.put("roleCode", "hrsc_dept_emp_info_mgr");
            } else {
                formData.put("roleCode", "hrsc_sub_emp_info_mgr");
            }

            formData.put("Honor", MyUtils.convertToMapList(changeReqVO));
            instanceDTO.setFormData(formData);
            log.info("流程测试instanceDTO" + instanceDTO);
            // 返回流程实例id
            ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
            log.info("流程测试processInstanceVo :{}" , JSON.toJSONString(processInstanceVo));

            // 5. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
            mongoTemplate.save(changeRequest);
            log.info("流程测试success");
            return new ResponseResult<>(HttpStatus.OK.value(), "变更申请已提交", changeRequest.getId());
        } catch (Exception e) {
            log.error("提交变更申请失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        log.info("requestId:" + requestId);
        requestId = MyUtils.subPk(requestId);
        log.info("新requestId:" + requestId);
        // 1. 从MongoDB获取变更请求
        HrEmpHonorChangeRequest request = mongoTemplate.findById(requestId, HrEmpHonorChangeRequest.class);
        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
        }

        // 由于附件信息不存在MongoDB中，先尝试进行查询并赋值
        List<HrEmpHonor> originalData = request.getOriginalData();
        List<HrEmpHonor> changedData = request.getChangedData();
        for(HrEmpHonor honor : originalData){
            hrEmpHonorService.resolveFiles(Collections.singletonList(honor));
        }
        for(HrEmpHonor honor : changedData){
            hrEmpHonorService.resolveFiles(Collections.singletonList(honor));
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
}
