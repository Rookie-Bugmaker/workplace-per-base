package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.api.WkWorkflowApi;
import com.crrc.entity.*;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mongodb.HrEmpEduChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpEducationDTO;
import com.crrc.service.impl.HrEmpEducationServiceImpl;
import com.crrc.service.impl.HrEmpMajorInfoServiceImpl;
import com.crrc.service.impl.HrEmpSchoolInfoServiceImpl;
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
 * 员工教育信息表 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpEducation")
@RefreshScope
@Tag(name = "员工教育信息表", description = "员工教育信息表")
public class HrEmpEducationController {

    @Autowired
    private HrEmpEducationServiceImpl hrEmpEducationService;
    @Autowired
    private HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService;
    @Autowired
    private HrEmpMajorInfoServiceImpl hrEmpMajorInfoService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;


    @GetMapping("/getSchoolInfo")
    @Operation(summary = "获取学校字典值")
    @AuditLog(value = "获取学校字典值", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<IPage<HrEmpSchoolInfo>> getSchoolInfo(
            @RequestParam(required = false) String schoolName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 1. 构造分页对象
        Page<HrEmpSchoolInfo> page = new Page<>(pageNum, pageSize);

        // 2. 构造模糊搜索条件（schoolName 为空时忽略）
        LambdaQueryWrapper<HrEmpSchoolInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(schoolName),
                HrEmpSchoolInfo::getSchoolName, schoolName);

        // 3. 直接分页查询
        IPage<HrEmpSchoolInfo> iPage = hrEmpSchoolInfoService.page(page, wrapper);

        return new ResponseResult<>(HttpStatus.OK.value(), "获取学校字典值成功", iPage);
    }


    @GetMapping("/getMajorInfo")
    @Operation(summary = "获取专业字典值")
    @AuditLog(value = "获取专业字典值", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<IPage<HrEmpMajorInfo>> getMajorInfo(
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 1. 构造分页对象
        Page<HrEmpMajorInfo> page = new Page<>(pageNum, pageSize);

        // 2. 构造模糊搜索条件（major 为空时忽略）
        LambdaQueryWrapper<HrEmpMajorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(major),
                HrEmpMajorInfo::getMajor, major);

        // 3. 直接分页查询
        IPage<HrEmpMajorInfo> iPage = hrEmpMajorInfoService.page(page, wrapper);

        return new ResponseResult<>(HttpStatus.OK.value(), "获取专业字典值成功", iPage);
    }


    @GetMapping("/getDetails")
    @Operation(summary = "获得员工教育信息表实体")
    @AuditLog(value = "获得员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<HrEmpEducation>> getMsg(@RequestParam String userCode) {
        List<HrEmpEducation> result = hrEmpEducationService.selectMsg(userCode);
        for (HrEmpEducation hrEmpEducation : result) {
            // 学校名称部分
            log.info("原始数据 - schoolCode: {}, schoolName: {}",
                    hrEmpEducation.getSchoolCode(),
                    hrEmpEducation.getSchoolName());

            // 修改判断逻辑
            if(StringUtils.isNotBlank(hrEmpEducation.getSchoolCode())) {
                HrEmpSchoolInfo hrEmpSchoolInfo = hrEmpSchoolInfoService.selectByCode(hrEmpEducation.getSchoolCode());
                log.info("查询结果: {}", hrEmpSchoolInfo);  // 打印整个对象

                String finalName = "未知院校";
                if(hrEmpSchoolInfo != null && StringUtils.isNotBlank(hrEmpSchoolInfo.getSchoolName())) {
                    finalName = hrEmpSchoolInfo.getSchoolName();
                }
                hrEmpEducation.setSchoolName(finalName);
            } else if(StringUtils.isBlank(hrEmpEducation.getSchoolName())) {
                hrEmpEducation.setSchoolName(hrEmpEducation.getSchoolName());
            }

            log.info("处理后 - schoolName: {}", hrEmpEducation.getSchoolName());

            // 专业部分
            if("001".equals(hrEmpEducation.getEducation()) || "002".equals(hrEmpEducation.getEducation())
                    || "003".equals(hrEmpEducation.getEducation()) || "004".equals(hrEmpEducation.getEducation())) {
                if(StringUtils.isNotBlank(hrEmpEducation.getMajorCode())) {
                    // 大专及以上学历,且学历编码不为空，进行学历回写
                    HrEmpMajorInfo hrEmpMajorInfo = hrEmpMajorInfoService.selectByCode(hrEmpEducation.getMajorCode());
                    hrEmpEducation.setMajor(hrEmpMajorInfo.getMajor());
                } else {
                    hrEmpEducation.setMajor("未知专业");
                }
            }else{
                // 其它学历，将专业置空
                hrEmpEducation.setMajor(null);
            }

            // 学位部分 todo
        }
        log.info("getDetails查询回来的教育经历原始数据：{}", JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表实体成功", result);
    }

    @PostMapping("/page")
    @Operation(summary = "获取员工教育信息表列表 page")
    @AuditLog(value = "获取员工教育信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<HrEmpEducation>> pageHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpEducation() : pageReqVO;
        Page<HrEmpEducation> page = new Page<>(pageNo, pageSize);
        hrEmpEducationService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取员工教育信息表列表")
    @AuditLog(value = "获取员工教育信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<HrEmpEducation>> queryHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpEducation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表列表成功", hrEmpEducationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得员工教育信息表实体")
    @AuditLog(value = "获得员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpEducation> getHrEmpEducation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工教育信息表实体成功", hrEmpEducationService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建员工教育信息表实体")
    @AuditLog(value = "创建员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpEducation(@Valid @RequestBody HrEmpEducation createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建员工教育信息表实体成功", hrEmpEducationService.save(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工教育信息表实体")
    @AuditLog(value = "更新员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<String> updateHrEmpEducation(@Valid @RequestBody HrEmpEducationDTO updateReqVO) {
        try {
            // 查询现有数据
            List<HrEmpEducation> existList = hrEmpEducationService.selectMsg(updateReqVO.getUserCode());
            Set<String> existIds = existList.stream()
                    .map(HrEmpEducation::getId)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<HrEmpEducation> toSaveOrUpdate = new ArrayList<>();

            // 判断传入数据里面有无最高学历
            boolean isHighestEdu = true;
            int isHighestNum = 0;
            for (HrEmpEducation hrEmpEducation : updateReqVO.getList()) {
                if (hrEmpEducation.getId() == null) {
                    // 生成新ID
                    hrEmpEducation.setId(IdUtil.simpleUUID());
                }
                // 判断最高学历
                if(SystemConstants.TRUE_FLAG.equals(hrEmpEducation.getIsHighestEducation())) {
                    isHighestNum++;
                    isHighestEdu = false;
                }
                // 当最高学历数量多余1时
                if(isHighestNum > 1) {
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "最高学历数量不唯一！");
                }
                hrEmpEducation.setUserCode(updateReqVO.getUserCode());
                toSaveOrUpdate.add(hrEmpEducation);
                // 移除不需要删除的ID
                existIds.remove(hrEmpEducation.getId());
            }
            // 没有最高学历,且教育信息列表不为空时
            if(isHighestEdu && !updateReqVO.getList().isEmpty()) {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "录入教育经历中未设置最高学历！");
            }
            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                hrEmpEducationService.saveOrUpdateBatch(toSaveOrUpdate);
            }
            // 批量删除
            if (!existIds.isEmpty()) {
                hrEmpEducationService.removeByIds(existIds);
            }
        } catch (Exception e) {
            log.error("更新或保存时出错：\n入参：{}\n错误日志：{}", updateReqVO, e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工教育信息表实体失败");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新员工教育信息表实体成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除员工教育信息表实体")
    @AuditLog(value = "删除员工教育信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpEducation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除员工教育信息表实体成功", hrEmpEducationService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出员工教育信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出员工教育信息表报表")
    public void exportHrEmpEducation(@Validated @RequestBody(required = false) HrEmpEducation exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("员工教育信息表报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpEducation() : exportReqVO;
        List<HrEmpEducation> hrEmpEducationList = hrEmpEducationService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpEducation.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("员工教育信息表报表")
                .doWrite(hrEmpEducationList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入员工教育信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入员工教育信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpEducation.class,
                new ImportExcelListener<HrEmpEducation, HrEmpEducationServiceImpl>(hrEmpEducationService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入员工教育信息表报表成功");
    }


    @PostMapping("/submit-change")
    @Operation(summary = "提交员工教育经历变更申请")
    public ResponseResult<String> submitChange(@Valid @RequestBody  HrEmpEducationDTO changeReqVO) {
        try {
            boolean flag = RoleUtils.check("人力资源部-招聘管理专员");
            if (flag) {
                log.info("权限校验通过，用户拥有'人力资源部-招聘管理专员'角色，直接进行更新");
                try{
                    updateHrEmpEducation(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'人力资源部-招聘管理专员'角色");
            // 1. 获取当前数据快照
            List<HrEmpEducation> originalData = getMsg(changeReqVO.getUserCode()).getData();
            log.info("提交审批流时查询回来的教育经历原始数据：{}", JSON.toJSONString(originalData, SerializerFeature.PrettyFormat));

            // 判断传入数据里面有无最高学历
            boolean isHighestEdu = true;
            int isHighestNum = 0;
            for (HrEmpEducation hrEmpEducation : changeReqVO.getList()) {
                // 判断最高学历
                if(SystemConstants.TRUE_FLAG.equals(hrEmpEducation.getIsHighestEducation())) {
                    isHighestNum++;
                    isHighestEdu = false;
                }
                // 当最高学历数量多余1时
                if(isHighestNum > 1) {
                    return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "最高学历数量不唯一！");
                }
                hrEmpEducation.setUserCode(changeReqVO.getUserCode());
            }
            // 没有最高学历,且教育信息列表不为空时
            if(isHighestEdu && !changeReqVO.getList().isEmpty()) {
                return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "录入教育经历中未设置最高学历！");
            }

            // 2. 创建变更申请文档
            HrEmpEduChangeRequest changeRequest = new HrEmpEduChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(changeReqVO.getUserCode());
            changeRequest.setOriginalData(originalData);
            // 在保存时刷写专业名称
            for(HrEmpEducation hrEmpEducation : changeReqVO.getList()) {
                // 专业部分
                if("001".equals(hrEmpEducation.getEducation()) || "002".equals(hrEmpEducation.getEducation())
                        || "003".equals(hrEmpEducation.getEducation()) || "004".equals(hrEmpEducation.getEducation())) {
                    if(StringUtils.isNotBlank(hrEmpEducation.getMajorCode())) {
                        // 大专及以上学历,且学历编码不为空，进行学历回写
                        HrEmpMajorInfo hrEmpMajorInfo = hrEmpMajorInfoService.selectByCode(hrEmpEducation.getMajorCode());
                        hrEmpEducation.setMajor(hrEmpMajorInfo.getMajor());
                    } else {
                        hrEmpEducation.setMajor("未知专业");
                    }
                }else{
                    // 其它学历，将专业置空
                    hrEmpEducation.setMajor(null);
                }
            }
            changeRequest.setChangedData(changeReqVO.getList());

            log.info("等待保存的教育经历变更数据：{}", JSON.toJSONString(changeReqVO.getList(), SerializerFeature.PrettyFormat));
            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);
            log.info("流程测试changeRequest" + changeRequest);


            // 4. 启动审批流程
            // 封装数据，提交审批流
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "EDU-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            // 封装数据主键，存在MongoDB里面的生成的simpleUUID
            instanceDTO.setBizId("edu_" +changeRequest.getId());
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
            String dept = hrEmpBasicMapper.selectOne(wrapper).getDept();
            Optional<Long> deptId = DeptUtils.getDeptIdByCode(dept);
            if(!deptId.isPresent()) {
                log.error("获取用户部门失败，部门查询错误，工号:" + loginUser.getUser().getJobNo());
                throw new IllegalArgumentException("获取用户部门失败，部门查询错误");
            }

            formData.put("deptId", deptId.get());
            formData.put("type","edu");
            formData.put("roleCode", "hrsc_audit");
            formData.put("Edu",MyUtils.convertToMapList(changeReqVO));
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
            log.error("提交变更申请失败: {}", e.getMessage());
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    @GetMapping("/change-detail")
    public ResponseResult<Map<String, Object>> getChangeDetail(@RequestParam String requestId) {
        log.info("requestId:" + requestId);
        requestId = MyUtils.subPk(requestId);
        log.info("新requestId:" + requestId);
        // 1. 从MongoDB获取变更请求
        HrEmpEduChangeRequest request = mongoTemplate.findById(requestId, HrEmpEduChangeRequest.class);
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
}
