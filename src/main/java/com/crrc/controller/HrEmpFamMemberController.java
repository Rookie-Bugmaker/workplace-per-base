package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpFamMember;
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
import com.crrc.mongodb.HrEmpFamMemberChangeRequest;
import com.crrc.mongodb.service.MongoDBService;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpFamMemberDTO;
import com.crrc.service.impl.HrEmpFamMemberServiceImpl;
import com.crrc.utils.*;
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
    @Operation(summary = "获得员工主要家庭成员社会关系表实体")
    @AuditLog(value = "获得员工主要家庭成员社会关系表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpFamMember>> getMsg(@RequestParam String userCode ) {
        List<HrEmpFamMember> famMembers = hrEmpFamMemberService.selectMsg(userCode);
        for(HrEmpFamMember famMember : famMembers) {
            // 处理政治面貌
            hrEmpFamMemberService.resolvePoliticalStatus(famMember);
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工主要家庭成员社会关系表实体成功", famMembers);
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工主要家庭成员社会关系表列表 page")
    @AuditLog(value = "获取员工主要家庭成员社会关系表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<?> pageHrEmpFamMember(@Validated @RequestBody(required = false) HrEmpFamMember pageReqVO,
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
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
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
                    // 生成新ID
                    hrEmpFamMember.setId(IdUtil.simpleUUID());
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
                // 移除不需要删除的ID
                existIds.remove(hrEmpFamMember.getId());
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

    @PostMapping("/tempSave")
    @Operation(summary = "暂存员工信息")
    public ResponseResult<String> tempSave(@RequestBody HrEmpFamMemberDTO saveDTO) {
        mongoDBService.saveDataByUserCode(saveDTO, saveDTO.getUserCode(), "hr_emp_fam_member_temp_save_data");
        return ResponseResult.success("OK");
    }

    @PostMapping("/queryData/{type}")
    @Operation(summary = "查询驳回/暂存的数据")
    public ResponseResult<HrEmpFamMemberDTO> tempQuery(@RequestBody HrEmpFamMemberDTO queryDTO, @PathVariable String type) {
        if("tempSave".equals(type)) {
            return ResponseResult.success(
                    mongoDBService.getById(queryDTO.getUserCode(), HrEmpFamMemberDTO.class, "hr_emp_fam_member_temp_save_data")
            );
        }

        return ResponseResult.success(mongoDBService.getById(queryDTO.getUserCode(), HrEmpFamMemberDTO.class, "hr_emp_fam_member_submit_data"));
    }

    @PostMapping("/submit-change")
    @Operation(summary = "提交员工家庭成员变更申请")
    public ResponseResult<String> submitChange(@Valid @RequestBody HrEmpFamMemberDTO changeReqVO) {
        try {
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try{
                    updateHrEmpFamMember(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'公司超级管理员'角色");
            // 1. 获取当前数据快照
            List<HrEmpFamMember> originalData = hrEmpFamMemberService.selectMsg(changeReqVO.getUserCode());
            log.info("提交审批流时查询回来的家庭成员原始数据：{}", JSON.toJSONString(originalData, SerializerFeature.PrettyFormat));

            // 检查是否存在正在修改中的数据
            for (HrEmpFamMember item : originalData) {
                if ("1".equals(item.getIsUpdating())) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // 如果没有数据，需要校验mongoDB中是否存在已有数据
            if (ObjectUtils.isEmpty(originalData)) {
                List<HrEmpFamMemberChangeRequest> reqs = mongoDBService.getByUserCode(changeReqVO.getUserCode(), HrEmpFamMemberChangeRequest.class, "hr_emp_famMem_change_requests");
                if (reqs.stream().anyMatch(e -> "PENDING".equals(e.getStatus()))) {
                    throw new IllegalArgumentException("请不要重复提交");
                }
            }

            // 2. 验证紧急联系人设置
            boolean isEmergency = true;
            int emergencyNum = 0;
            for (HrEmpFamMember member : changeReqVO.getList()) {
                // 判断紧急联系人
                if(SystemConstants.TRUE_FLAG.equals(member.getIsEmergencyContact())) {
                    emergencyNum++;
                    isEmergency = false;
                }
                // 当紧急联系人数量多余1时
                if(emergencyNum > 1) {
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "紧急联系人数量不唯一！");
                }
                member.setUserCode(changeReqVO.getUserCode());
            }
            // 没有紧急联系人,且家庭成员列表不为空时
            if(isEmergency && !changeReqVO.getList().isEmpty()) {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "录入员工主要家庭成员中未设置紧急联系人！");
            }

            // 3. 创建变更申请文档
            HrEmpFamMemberChangeRequest changeRequest = new HrEmpFamMemberChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);

            // 全表扫描对比字段变化
            diffResolver.resolveDifference(originalData, changeReqVO.getList(), HrEmpFamMember.class);

            changeRequest.setChangedData(changeReqVO.getList());

            // 4. 保存到MongoDB
            mongoTemplate.save(changeRequest);
            log.info("家庭成员变更请求已保存: {}", changeRequest);

            // 保存到MongoDB之后再保存暂存数据
            mongoDBService.saveDataByUserCode(changeReqVO, changeReqVO.getUserCode(), "hr_emp_fam_member_submit_data");

            // 5. 启动审批流程
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "FAM-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            instanceDTO.setBizId("family_" + changeRequest.getId());
            instanceDTO.setTemplateAlias(SystemConstants.PROCESS_TEMPLATE_ALIAS);

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
            formData.put("type", "family");

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

            formData.put("FamilyMembers", MyUtils.convertToMapList(changeReqVO.getList()));
            instanceDTO.setFormData(formData);
            log.info("流程测试instanceDTO" + instanceDTO);

            ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
            log.info("流程测试processInstanceVo" + processInstanceVo);
            // 6. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
            mongoTemplate.save(changeRequest);
            log.info("流程测试success");

            return new ResponseResult<>(HttpStatus.OK.value(), "家庭成员变更申请已提交", changeRequest.getId());
        } catch (Exception e) {
            log.error("提交家庭成员变更申请失败: {}", e.getMessage(), e);
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        try {
            log.info("家庭成员变更请求ID: {}", requestId);
            requestId = MyUtils.subPk(requestId);

            // 1. 从MongoDB获取变更请求
            HrEmpFamMemberChangeRequest request = mongoTemplate.findById(requestId, HrEmpFamMemberChangeRequest.class);
            if (request == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "变更申请不存在");
            }

            // 处理单据中的家庭成员的政治面貌
            List<HrEmpFamMember> originalData = request.getOriginalData();
            List<HrEmpFamMember> changedData = request.getChangedData();

            for (HrEmpFamMember member : originalData) {
                hrEmpFamMemberService.resolvePoliticalStatus(member);
            }

            for (HrEmpFamMember member : changedData) {
                hrEmpFamMemberService.resolvePoliticalStatus(member);
            }

            // 2. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("originalData", MyUtils.convertToMapList(originalData));
            result.put("changedData", MyUtils.convertToMapList(changedData));
            result.put("requestId", requestId);
            result.put("createTime", request.getCreateTime());

            return new ResponseResult<>(HttpStatus.OK.value(), "成功", result);
        } catch (Exception e) {
            log.error("获取家庭成员变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }
}
