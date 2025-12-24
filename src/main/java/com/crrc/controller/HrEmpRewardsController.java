package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkDataAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpRewards;
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
import com.crrc.mongodb.HrEmpRewardsChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.UpdateListDTO;
import com.crrc.service.impl.HrEmpRewardsServiceImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 员工奖励情况表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpRewards")
@RefreshScope
@Tag(name = "员工奖励情况表", description = "员工奖励情况表")
@Slf4j
public class HrEmpRewardsController {

    @Autowired
    private HrEmpRewardsServiceImpl hrEmpRewardsService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;

    private final UpdateListUtil<HrEmpRewards, String> updateUtil;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    private WkDataAPi wkDataAPi;

    public HrEmpRewardsController(HrEmpRewardsServiceImpl service) {
        this.updateUtil = new UpdateListUtil<>(
                service::selectMsg,          // 查询方法
                HrEmpRewards::getId,      // ID获取方法
                HrEmpRewards::setUserCode,// userCode设置方法
                HrEmpRewards::setId,      // ID设置方法
                service::saveOrUpdateBatch,  // 保存/更新方法
                service::removeByIds         // 删除方法
        );
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工奖励情况实体")
    @AuditLog(value = "获得员工奖励情况实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpRewards>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工奖励情况实体成功", hrEmpRewardsService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工奖励情况表列表 page")
    @AuditLog(value = "获取员工奖励情况表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpRewards(@Validated @RequestBody(required = false) HrEmpRewards pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpRewards() : pageReqVO;
        Page<HrEmpRewards> page = new Page<>(pageNo, pageSize);
        hrEmpRewardsService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工奖励情况表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工奖励情况表列表")
    @AuditLog(value = "获取员工奖励情况表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpRewards>> queryHrEmpRewards(@Validated @RequestBody(required = false) HrEmpRewards queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpRewards() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工奖励情况表列表成功", hrEmpRewardsService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工奖励情况表实体")
    @AuditLog(value = "获得员工奖励情况表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpRewards> getHrEmpRewards(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工奖励情况表实体成功", hrEmpRewardsService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工奖励情况表实体")
    @AuditLog(value = "创建员工奖励情况表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpRewards(@Valid @RequestBody HrEmpRewards createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工奖励情况表实体成功", hrEmpRewardsService.save(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工奖励情况表实体")
    @AuditLog(value = "更新员工奖励情况表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "updateReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpRewards(@Valid @RequestBody UpdateListDTO<HrEmpRewards> updateReqVO) {
        return updateUtil.update(updateReqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工奖励情况表实体")
    @AuditLog(value = "删除员工奖励情况表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpRewards(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工奖励情况表实体成功", hrEmpRewardsService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工奖励情况表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工奖励情况表报表")
    public void exportHrEmpRewards(@Validated @RequestBody(required = false) HrEmpRewards exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工奖励情况表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpRewards() : exportReqVO;
        List<HrEmpRewards> hrEmpRewardsList = hrEmpRewardsService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpRewards.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工奖励情况表报表")
                .doWrite(hrEmpRewardsList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工奖励情况表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工奖励情况表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpRewards.class,
                new ImportExcelListener<HrEmpRewards, HrEmpRewardsServiceImpl>(hrEmpRewardsService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工奖励情况表报表成功");
    }

    @PostMapping("/submit-change")
    @Operation(summary = "提交员工奖励情况变更申请")
    public ResponseResult<String> submitRewardChange(@Valid @RequestBody UpdateListDTO<HrEmpRewards> changeReqVO) {
        try {
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
                try{
                    updateHrEmpRewards(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'公司超级管理员'角色");
            // 1. 获取当前数据快照
            List<HrEmpRewards> originalData = hrEmpRewardsService.selectMsg(changeReqVO.getUserCode());
            log.info("员工奖励情况原始数据：{}", JSON.toJSONString(originalData));

            // 2. 创建变更申请文档
            HrEmpRewardsChangeRequest changeRequest = new HrEmpRewardsChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);
            changeRequest.setChangedData(changeReqVO.getList());
            changeRequest.setStatus("PENDING");

            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);

            // 4. 启动审批流程
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "RWD-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            instanceDTO.setBizId("rewards_" + changeRequest.getId());
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
            formData.put("type", "rewards");

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

            formData.put("EmployeeRewards", MyUtils.convertToMapList(changeReqVO.getList()));
            instanceDTO.setFormData(formData);

            ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);

            // 5. 更新流程实例ID
            changeRequest.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
            mongoTemplate.save(changeRequest);

            return new ResponseResult<>(HttpStatus.OK.value(), "员工奖励情况变更申请已提交", changeRequest.getId());
        } catch (Exception e) {
            log.error("提交员工奖励情况变更申请失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    // 获取变更详情的Controller方法
    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getRewardChangeDetail(@RequestParam String requestId) {
        try {
            requestId = MyUtils.subPk(requestId);
            HrEmpRewardsChangeRequest request = mongoTemplate.findById(requestId, HrEmpRewardsChangeRequest.class);

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
            log.error("获取员工奖励情况变更详情失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取详情失败");
        }
    }
}
