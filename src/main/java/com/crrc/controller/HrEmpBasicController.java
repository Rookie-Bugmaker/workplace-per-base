package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.client.UserProfileImageClient;
import com.crrc.entity.*;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.mapper.HrEmpCareerPathMapper;
import com.crrc.mapper.HrEmpOtherAppointmentMapper;
import com.crrc.mapper.HrEmpResumeMapper;
import com.crrc.mongodb.HrEmpBasicChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.*;
import com.crrc.pojo.vo.MapVO;
import com.crrc.service.CodeInfoService;
import com.crrc.service.impl.*;
import com.crrc.utils.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
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
import org.springframework.util.ObjectUtils;
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
    @Autowired
    private HrEmpCareerPathMapper hrEmpCareerPathMapper;
    @Autowired
    private HrEmpResumeMapper hrEmpResumeMapper;
    @Autowired
    private HrEmpAppointmentMapper hrEmpAppointmentMapper;
    @Autowired
    private HrEmpOtherAppointmentMapper hrEmpOtherAppointmentMapper;
    @Autowired
    private HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;
    @Autowired
    private CodeInfoService codeInfoService;
    @Autowired
    private UserProfileImageClient userProfileImageClient;
    @Autowired
    private MinIOUtils minioUtils;
    @Autowired
    private WkDataAPi wkDataAPi;

    @GetMapping("/isAvailable")
    @Operation(summary = "就绪探针")
    public ResponseResult<String> isAvailable() {
        return new ResponseResult<>(HttpStatus.OK.value(), "OK", "OK");
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/syncDept")
    public ResponseResult<String> syncDept(@RequestBody List<String> userCodeList) {

        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        if(userCodeList != null && !userCodeList.isEmpty()) {
            queryWrapper.in(HrEmpBasic::getUserCode, userCodeList);
        }

        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.getBaseMapper().selectList(queryWrapper);
        int count = 0;
        for(HrEmpBasic basic : hrEmpBasicList) {
            LambdaQueryWrapper<HrEmpAppointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpAppointment::getUserCode, basic.getUserCode());
            List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(wrapper);
            Optional<HrEmpAppointment> max = hrEmpAppointments.stream().filter(e -> e.getStartDate() != null).max(Comparator.comparing(HrEmpAppointment::getStartDate));
            if(max.isPresent() && !Objects.equals(basic.getDept(), max.get().getDept())) {
                basic.setDept(max.get().getDept());
                hrEmpBasicService.saveOrUpdate(basic);
            }
            count++;
            log.info("进度: {}", (float) count/ (float) hrEmpBasicList.size());
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK");
    }

    @PostMapping("/syncPicture")
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> syncPicture() {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(HrEmpBasic::getProfilePicture);
        queryWrapper.isNull(HrEmpBasic::getAttendanceImageId);
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.getBaseMapper().selectList(queryWrapper);

        List<String> userCodeList = hrEmpBasicList.stream().map(HrEmpBasic::getUserCode).collect(Collectors.toList());
        log.info("userCodeList长度：{}", userCodeList.size());
        if(userCodeList.isEmpty()) {
            return new ResponseResult<>(HttpStatus.OK.value(), "OK", true);
        }
        log.info("准备开始分片");
        // 分批请求
        List<List<String>> partition = ListUtils.partition(userCodeList, 20);
        log.info("开始分批请求");
        int count = 0;
        for(List<String> uuidList : partition) {
            log.info("第{}批: {}", count++, uuidList);
            ImageReqDTO imageReqDTO = new ImageReqDTO();
            imageReqDTO.setUuidList(uuidList);
            ResponseData<List<UserImageDTO>> personImageByUserCode = userProfileImageClient.getPersonImageByUserCode(imageReqDTO);

            List<UserImageDTO> data = personImageByUserCode.getData();

            data.stream().filter(e -> StringUtils.isNotBlank(e.getImageUri())).forEach(userImageDTO -> {
                try {
                    String imageUri = userImageDTO.getImageUri();
                    // 获取数据流
                    byte[] bytes = ImageUtils.downloadImage(imageUri);
                    // 转为MultipartFile
                    ByteArrayMultipartFile image = new ByteArrayMultipartFile(bytes, "image", "image.jpg", "image/jpeg");
                    // 上传到minIO
                    String profilePicture = minioUtils.saveFile(image);
                    HrEmpBasic userInfo = hrEmpBasicService.getByUserCode(userImageDTO.getCode());
                    userInfo.setProfilePicture(profilePicture);
                    // 更新数据
                    hrEmpBasicService.updateById(userInfo);
                    log.info("save by id: {}, profilePicture: {}", userInfo.getId(), profilePicture);
                } catch (Exception e) {
                    log.info("download or upload error: {}, {}", userImageDTO, uuidList, e);
                }
            });

        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", true);
    }

    @PostMapping("/upload/{userCode}")
    @Operation(summary = "图片上传")
    @AuditLog(value = "图片上传", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<String> uploadPicture(@PathVariable String userCode, MultipartFile imageFile) {
        if(StringUtils.isBlank(userCode)) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "未传入档案号");
        }
        if(ObjectUtils.isEmpty(imageFile)) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "未传入图片");
        }
        // 上传到minIO
        String profilePicture = minioUtils.saveFile(imageFile);

        boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin");
        if (flag) {
            log.info("权限校验通过，用户拥有'公司超级管理员'角色，直接进行更新");
            try{
                // 构建更新条件
                MPJLambdaWrapper<HrEmpBasic> updateWrapper = new MPJLambdaWrapper<>();
                updateWrapper.eq(HrEmpBasic::getUserCode, userCode);

                // 构建更新实体
                HrEmpBasic updateEntity = new HrEmpBasic();
                updateEntity.setProfilePicture(profilePicture);

                // 执行更新
                boolean updateResult = hrEmpBasicService.update(updateEntity, updateWrapper);

                if(updateResult) {
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } else {
                    return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新失败");
                }
            } catch (Exception e){
                log.warn("权限校验成功，但直接更新失败，尝试走审批流", e);
            }
        }
        log.warn("权限校验失败，用户缺少'公司超级管理员'角色");

        try{
            // 1. 获取当前数据快照
            HrEmpBasicDTO changeReqVO = new HrEmpBasicDTO();
            changeReqVO.setUserCode(userCode);
            changeReqVO.setProfilePicture(profilePicture);

            HrEmpBasic original = hrEmpBasicService.getByUserCode(userCode);
            HrEmpBasicDTO originalData = new HrEmpBasicDTO();
            originalData.setUserCode(userCode);
            originalData.setProfilePicture(original.getProfilePicture());

            // 2. 创建变更申请文档
            HrEmpBasicChangeRequest changeRequest = new HrEmpBasicChangeRequest();
            changeRequest.setId(IdUtil.simpleUUID());
            changeRequest.setUserCode(userCode);
            changeRequest.setOriginalData(originalData);
            changeRequest.setChangedData(changeReqVO);
            changeRequest.setWfId(changeReqVO.getWfId());

            // 3. 保存到MongoDB
            mongoTemplate.save(changeRequest);
            log.info("流程测试changeRequest" + changeRequest);

            // 4. 启动审批流程
            // 封装数据，提交审批流
            StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
            String bizCode = "PIC-" + System.currentTimeMillis();
            instanceDTO.setBizCode(bizCode);
            // 封装数据主键，存在MongoDB里面的生成的simpleUUID
            instanceDTO.setBizId("pic_" + changeRequest.getId());
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

            String deptCode = hrEmpBasicService.getDept(loginUser.getUser().getJobNo());

            if(StringUtils.isBlank(deptCode)) {
                log.error("获取用户部门失败，部门查询错误，工号:" + loginUser.getUser().getJobNo());
                throw new IllegalArgumentException("获取用户部门失败，部门查询错误");
            }


            formData.put("deptCode", deptCode);
            formData.put("type","pic");
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

            formData.put("Pic",MyUtils.convertToMapList(changeReqVO));
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
            log.error("详细信息", e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "提交失败");
        }
    }

    @GetMapping("/download")
    @Operation(summary = "图片下载")
    @AuditLog(value = "图片下载", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<String> downloadPicture(@RequestParam String fileId) {
        return new ResponseResult<>(HttpStatus.OK.value(), "OK", minioUtils.getFileUrl(fileId));
    }

    @GetMapping("/getDept")
    @Operation(summary = "获得员工所属部门")
    @AuditLog(value = "获得员工所属部门", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<String> getDept(@RequestParam String userCode) {
        try {
            String dept = hrEmpBasicService.getDept(userCode);
            if(StringUtils.isNotBlank(dept)){
                return new ResponseResult<>(HttpStatus.OK.value(), "获得员工所属部门成功", dept);
            } else {
                log.warn("查询回的部门为空，默认返回流程与数字化部，档案号为{}", userCode);
                return new ResponseResult<>(HttpStatus.OK.value(), "查询回的部门为空，使用默认部门流程与数字化部", SystemConstants.DEFAULT_DEPT_CODE);
            }
        } catch (Exception e) {
            log.error("获得员工所属部门失败{}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.OK.value(), "获得员工所属部门失败，使用默认部门流程与数字化部", SystemConstants.DEFAULT_DEPT_CODE);
        }
    }

    @PostMapping("/syncPersonCategory")
    @Operation(summary = "同步人员分类，目前用定时任务每天处理")
    @AuditLog(value = "同步人员分类，目前用定时任务每天处理", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @XxlJob("syncPersonCategory")
    public ResponseResult<String> syncPersonCategory(@RequestBody(required = false) List<HrEmpBasic> userCodeList) {
        // 现在人员分类4不需要计算，通过金蝶直接传参解决
        try {
            // 如果传参了且非空，就用传参，否则全量更新
            List<HrEmpBasic> list = (userCodeList != null && !userCodeList.isEmpty())
                    ? hrEmpBasicService.getListByUserCode(userCodeList)
                    : hrEmpBasicService.getAllUser();

            Map<String, String> position2ValueMap = codeInfoService.getTranslateMap("hr_emp_appointment", "APPOINTMENT_POSITION");

            for(HrEmpBasic hrEmpBasic : list){

                // 同步人员分类1
                String category1 = resolveCategory1(hrEmpBasic);
                hrEmpBasic.setCategory1(category1);
                log.info("userCode:{},查询人员分类1完毕,值为{}", hrEmpBasic.getUserCode(), category1);
                // 同步人员分类4
//                String category4 = resolveCategory4(hrEmpBasic, position2ValueMap);
//                hrEmpBasic.setCategory4(category4);
//                log.info("userCode:{},查询人员分类4完毕,值为{}", hrEmpBasic.getUserCode(), category4);
                // 同步人员分类7
                String category7 = resolveCategory7(hrEmpBasic);
                hrEmpBasic.setCategory7(category7);
                log.info("userCode:{},查询人员分类7完毕,值为{}", hrEmpBasic.getUserCode(), category7);
                // 同步人员分类8
                String category8 = resolveCategory8(hrEmpBasic);
                hrEmpBasic.setCategory8(category8);
                log.info("userCode:{},查询人员分类8完毕,值为{}", hrEmpBasic.getUserCode(), category8);

                // 更新方法
                log.info("准备执行更新方法，category1:{}，category7:{}，category8:{}",
                        hrEmpBasic.getCategory1(), hrEmpBasic.getCategory7() ,hrEmpBasic.getCategory8());
                LambdaUpdateWrapper<HrEmpBasic> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(HrEmpBasic::getUserCode, hrEmpBasic.getUserCode())
                        .set(HrEmpBasic::getCategory1, hrEmpBasic.getCategory1())
//                        .set(HrEmpBasic::getCategory4, hrEmpBasic.getCategory4())
                        .set(HrEmpBasic::getCategory7, hrEmpBasic.getCategory7())
                        .set(HrEmpBasic::getCategory8, hrEmpBasic.getCategory8());
                hrEmpBasicService.update(wrapper);
                log.info("userCode:{},更新成功", hrEmpBasic.getUserCode());
            }
            return new ResponseResult<>(HttpStatus.OK.value(), "人员分类同步成功");
        } catch (Exception e) {
            log.error("人员分类同步失败，{}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "人员分类同步失败" + e.getMessage());
        }
    }

    @PostMapping("/syncPersonCategory7")
    @Operation(summary = "同步人员分类，目前用定时任务每天处理")
    @AuditLog(value = "同步人员分类，目前用定时任务每天处理", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @XxlJob("syncPersonCategory7")
    public ResponseResult<String> syncPersonCategory7(@RequestBody(required = false) List<HrEmpBasic> userCodeList) {
        // 现在人员分类4不需要计算，通过金蝶直接传参解决
        try {
            // 如果传参了且非空，就用传参，否则全量更新
            List<HrEmpBasic> list = (userCodeList != null && !userCodeList.isEmpty())
                    ? hrEmpBasicService.getListByUserCode(userCodeList)
                    : hrEmpBasicService.getAllUser();

            for(HrEmpBasic hrEmpBasic : list){
                // 同步人员分类7
                String category7 = resolveCategory7(hrEmpBasic);
                log.info("userCode:{},查询人员分类7完毕,值为{}", hrEmpBasic.getUserCode(), category7);
                // 更新方法
                log.info("准备执行更新方法，category7:{}", category7);
                LambdaUpdateWrapper<HrEmpBasic> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(HrEmpBasic::getUserCode, hrEmpBasic.getUserCode())
                        .set(HrEmpBasic::getCategory7, category7);
                hrEmpBasicService.update(wrapper);
                log.info("userCode:{},更新成功", hrEmpBasic.getUserCode());
            }
            return new ResponseResult<>(HttpStatus.OK.value(), "人员分类同步成功");
        } catch (Exception e) {
            log.error("人员分类同步失败，{}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "人员分类同步失败" + e.getMessage());
        }
    }

    private String resolveCategory1(HrEmpBasic hrEmpBasic) {
        try {
            String category1 = null;
            // 同步人员分类1，根据基本信息的身份类型判断人员分类1，
            // 员工基本信息→身份类型→干部  干
            // 员工基本信息→身份类型→工人  工
            if (StringUtils.isNotBlank(hrEmpBasic.getIdentityType())) {
                log.info("当前身份类型{}", hrEmpBasic.getIdentityType());
                switch (hrEmpBasic.getIdentityType()) {
                    // 身份类型 干部
                    case "1":
                    case "干部":
                        // 人员分类1 “干”对应的码值
                        category1 = "001";
                        break;

                    // 身份类型 工人
                    case "2":
                    case "工人":
                        // 人员分类1 “工”对应的码值
                        category1 = "002";
                        break;

                    default:
                }
            }
            return category1;
        } catch (Exception e) {
            log.error("人员分类1同步失败: {}", e.getMessage(), e);
            return hrEmpBasic.getCategory1();
        }
    }

    private String resolveCategory7(HrEmpBasic hrEmpBasic) {
        try {
            String category7_pro = null;
            // 获取专业职务任职资格列表
            List<HrEmpProfessionalQualification> qualificationList = hrEmpProfessionalQualificationService.selectMsg(hrEmpBasic.getUserCode());
            if (!qualificationList.isEmpty()) {
                for (HrEmpProfessionalQualification qualification : qualificationList) {
                    // 找到目前最高的任职资格，以此为判断标准
                    if (StringUtils.isNotBlank(qualification.getIsHighest()) && SystemConstants.TRUE_FLAG.equals(qualification.getIsHighest()) && StringUtils.isNotBlank(qualification.getQuaTitleLevel())) {
                        if(qualification.getQuaTitleLevel().equals("00010") || qualification.getQuaTitleLevel().contains("正高级")) {
                            category7_pro = "001";
                        } else if(qualification.getQuaTitleLevel().equals("00020") || qualification.getQuaTitleLevel().contains("高级")) {
                            category7_pro = "002";
                        } else if(qualification.getQuaTitleLevel().equals("00030") || qualification.getQuaTitleLevel().contains("中级")) {
                            category7_pro = "003";
                        } else if(qualification.getQuaTitleLevel().equals("00040") || qualification.getQuaTitleLevel().contains("助理级") || qualification.getQuaTitleLevel().contains("初级")) {
                            category7_pro = "004";
                        } else if(qualification.getQuaTitleLevel().equals("00050") || qualification.getQuaTitleLevel().contains("员级")) {
                            category7_pro = "005";
                        }
                    }
                }
            }

            String category7_skl = null;
            // 职业技能等级
            List<HrEmpSkillLevel> skillLevelList = hrEmpSkillLevelService.selectMsg(hrEmpBasic.getUserCode());
            if (!skillLevelList.isEmpty()) {
                for (HrEmpSkillLevel skillLevel : skillLevelList) {
                    // 找到目前最高的任职资格，以此为判断标准
                    if (StringUtils.isNotBlank(skillLevel.getIsHighest()) && SystemConstants.TRUE_FLAG.equals(skillLevel.getIsHighest())) {
                        if (StringUtils.isNotBlank(skillLevel.getSkillLevel())) {
                                                     /*
                                  00010/0020/0030	高级技师、特级技师、首席技师 高
                                  00040	技师 师
                                  00050	高级工 助
                                  00060/70	中级工、初级工 员
                                 */
                            if(skillLevel.getSkillLevel().equals("00010") || skillLevel.getSkillLevel().contains("首席技师")) {
                                category7_skl = "001";
                            } else if(skillLevel.getSkillLevel().equals("00020") || skillLevel.getSkillLevel().contains("特级技师")) {
                                category7_skl = "001";
                            } else if(skillLevel.getSkillLevel().equals("00030") || skillLevel.getSkillLevel().contains("高级技师")) {
                                category7_skl = "001";
                            } else if(skillLevel.getSkillLevel().equals("00040") || skillLevel.getSkillLevel().contains("技师")) {
                                category7_skl = "002";
                            } else if(skillLevel.getSkillLevel().equals("00050") || skillLevel.getSkillLevel().contains("高级工")) {
                                category7_skl = "003";
                            } else if(skillLevel.getSkillLevel().equals("00060") || skillLevel.getSkillLevel().contains("中级工")) {
                                category7_skl = "004";
                            } else if(skillLevel.getSkillLevel().equals("00070") || skillLevel.getSkillLevel().contains("初级工")) {
                                category7_skl = "005";
                            }
                        }
                        // 如果新加了码值，请处理这里的技师判断逻辑
                        return CodeValueComparatorUtils.compareCategory7(category7_pro, category7_skl) > 0 ? category7_skl : category7_pro;
                    }
                }
            }

            // 如果是双通道评聘，取高的一条
            if(category7_pro != null && category7_skl != null) {
                return Integer.parseInt(category7_pro) < Integer.parseInt(category7_skl) ? category7_pro : category7_skl;
            }
            return null;
        } catch (Exception e) {
            log.error("人员分类7同步失败: {}", e.getMessage(), e);
            return hrEmpBasic.getCategory7();
        }
    }

    private String resolveCategory8(HrEmpBasic hrEmpBasic) {
        try {
            // 先判断借调、外派、助勤
            // 查找duty_status不为空的其他任职信息
            LambdaQueryWrapper<HrEmpOtherAppointment> otherAppQueryWrapper = new LambdaQueryWrapper<>();
            otherAppQueryWrapper.eq(HrEmpOtherAppointment::getUserCode, hrEmpBasic.getUserCode());
            List<HrEmpOtherAppointment> hrEmpOtherAppointments = hrEmpOtherAppointmentMapper.selectList(otherAppQueryWrapper).stream()
                    .filter(e -> e.getDutyStatus() != null).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(hrEmpOtherAppointments)) {
                log.info("开始判断其他任职信息(借、派、助)");
                HrEmpOtherAppointment hrEmpOtherAppointment = hrEmpOtherAppointments.get(0);
                // 如果超过一条，选择startDate最晚的
                if (hrEmpOtherAppointments.size() > 1) {
                    hrEmpOtherAppointment = hrEmpOtherAppointments.stream().reduce(
                            (a, b) -> a.getStartDate().isAfter(b.getStartDate()) ? a : b
                    ).get();
                }
                String appointmentType = hrEmpOtherAppointment.getOtherAppointmentType();
                // 其他任职信息→其他任职类型→借调开始 -> 借
                if ("022".equals(appointmentType) || "借调开始".equals(appointmentType)) {
                    return "002";
                }
                // 其他任职信息→其他任职类型→外派开始 -> 派
                if ("028".equals(appointmentType) || "外派开始".equals(appointmentType)) {
                    return "004";
                }
                // 其他任职信息→其他任职类型→助勤开始 -> 助
                if ("031".equals(appointmentType) || "助勤开始".equals(appointmentType)) {
                    return "008";
                }
                log.info("其他任职信息(借、派、助)均不匹配");
            }

            LambdaQueryWrapper<HrEmpAppointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
            appointmentQueryWrapper.eq(HrEmpAppointment::getUserCode, hrEmpBasic.getUserCode());
            // 查找duty_status不为空的任职信息
            List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(appointmentQueryWrapper).stream()
                    .filter(e -> e.getDutyStatus() != null).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(hrEmpAppointments)) {
                log.info("开始判断任职信息(待、劳、伤、现、养)");
                HrEmpAppointment hrEmpAppointment = hrEmpAppointments.get(0);
                // 如果超过一条，选择startDate最晚的
                if (hrEmpAppointments.size() > 1) {
                    hrEmpAppointment = hrEmpAppointments.stream().filter(e -> e.getStartDate() != null).reduce((a, b) -> a.getStartDate().isAfter(b.getStartDate()) ? a : b).get();
                }
                String dutyStatus = hrEmpAppointment.getDutyStatus();
                // 任职信息→在岗状态→待岗 -> 待
                if ("待岗".equals(dutyStatus) || "201".equals(dutyStatus)) {
                    return "001";
                }
                // 任职信息→在岗状态→因病长期休假人员 -> 劳
                if ("因病长期休假".equals(dutyStatus) || "302".equals(dutyStatus)) {
                    return "003";
                }
                // 任职信息→在岗状态→因伤长期休假人员 -> 伤
                if ("因伤长期休假".equals(dutyStatus) || "303".equals(dutyStatus)) {
                    return "005";
                }
                // 任职信息→在岗状态→在岗 -> 现
                if ("在岗".equals(dutyStatus) || "101".equals(dutyStatus)) {
                    return "006";
                }  else if(!ObjectUtils.isEmpty(hrEmpOtherAppointments)){
                    // 其它任职信息→在岗状态→在岗 -> 现
                    HrEmpOtherAppointment hrEmpOtherAppointment = hrEmpOtherAppointments.get(0);
                    // 如果超过一条，选择startDate最晚的
                    if (hrEmpOtherAppointments.size() > 1) {
                        hrEmpOtherAppointment = hrEmpOtherAppointments.stream().reduce(
                                (a, b) -> a.getStartDate().isAfter(b.getStartDate()) ? a : b
                        ).get();
                    }
                    String otherAppointmentDutyStatus = hrEmpOtherAppointment.getDutyStatus();
                    if ("在岗".equals(otherAppointmentDutyStatus) || "101".equals(otherAppointmentDutyStatus)) {
                        return "006";
                    }
                }
                // 任职信息→在岗状态→859内退人员&“三类精算”内退人员&新增精算内退人员&其他内退人员 -> 养
                if ("859内退人员".equals(dutyStatus) || "“三类精算”内退人员".equals(dutyStatus) || "新增精算内退人员".equals(dutyStatus) || "其他内退人员".equals(dutyStatus)
                        || "202".equals(dutyStatus) || "203".equals(dutyStatus) || "204".equals(dutyStatus) || "205".equals(dutyStatus)) {
                    return "007";
                }
                log.info("任职信息(待、劳、伤、现、养)均不匹配");
            }
            // 全都匹配不上，返回空
            return null;
        } catch (Exception e) {
            log.error("人员分类8同步错误: {}", e.getMessage(), e);
            return hrEmpBasic.getCategory8();
        }
    }

    private String resolveCategory4(HrEmpBasic hrEmpBasic, Map<String, String> position2ValueMap) {
        try {
            // 同步人员分类4，根据基本信息的身份类型判断人员分类4
            LambdaQueryWrapper<HrEmpCareerPath> pathQueryWrapper = new LambdaQueryWrapper<>();
            pathQueryWrapper.eq(HrEmpCareerPath::getUserCode, hrEmpBasic.getUserCode());
            List<HrEmpCareerPath> hrEmpCareerPaths = hrEmpCareerPathMapper.selectList(pathQueryWrapper);
            if (!ObjectUtils.isEmpty(hrEmpCareerPaths)) {
                // 找到经营管理通道的list
                // 001	经营管理
                HrEmpCareerPath path = hrEmpCareerPaths.stream().filter(item -> item.getPathId() != null).max(Comparator.comparing(HrEmpCareerPath::getEffectiveDate)).orElse(null);
                if (path != null && ("001".equals(path.getPathId()) || path.getPathId().contains("经营管理通道"))) {
                    // 经营管理职类，经营管理通道，等级D1/D2 -> 厂
                    String levelId = path.getLevelId();
                    if ("D1".equals(levelId) || "D2".equals(levelId)) {
                        return "001";
                    }
                    // 经营管理职类，经营管理通道，等级M1/M2/M3 -> 中
                    if ("M1".equals(levelId) || "M2".equals(levelId) || "M3".equals(levelId)) {
                        return "004";
                    }
                }
            }

            LambdaQueryWrapper<HrEmpResume> resumeQueryWrapper = new LambdaQueryWrapper<>();
            resumeQueryWrapper.eq(HrEmpResume::getUserCode, hrEmpBasic.getUserCode());
            List<HrEmpResume> hrEmpResumeList = hrEmpResumeMapper.selectList(resumeQueryWrapper);
            String position = null;
            if (!ObjectUtils.isEmpty(hrEmpResumeList)) {
                // 找到endDate为空的，即现任岗位
                List<HrEmpResume> hrEmpResumes = hrEmpResumeList.stream().filter(item -> item.getEndDate() == null)
                        .filter(item -> !ObjectUtils.isEmpty(item.getStartDate())).collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(hrEmpResumes)) {
                    // 如果endDate有多个不为空，找到startDate最晚的
                    HrEmpResume hrEmpResume = hrEmpResumes.get(0);
                    if (hrEmpCareerPaths.size() > 1) {
                        hrEmpResume = hrEmpResumes.stream().reduce((a, b) -> a.getStartDate().isAfter(b.getStartDate()) ? a : b).get();
                    }
                    // 获取职位名称（从码值到汉字）
                    position = position2ValueMap.getOrDefault(hrEmpResume.getPosition(), hrEmpResume.getPosition());
                    // 岗位名称“协理（临时）” -> 二
                    if ("协理（临时）".equals(position)) {
                        return "003";
                    }
                    // 岗位名称“高级顾问/顾问” -> 厂二
                    if ("高级顾问".equals(position) || "顾问".equals(position) || "高级顾问/顾问".equals(position) || "434".equals(position) || "435".equals(position)) {
                        return "002";
                    }
                    // 岗位名称“级经理” -> 干业
                    // 匹配 一级经理等等
                    if (position != null && position.contains("  ")) {
                        return "005";
                    }
                    // 岗位名称“级业务经理” -> 人业
                    if (position != null && position.contains("级业务经理")) {
                        return "006";
                    }
                }
            }

            if (position != null) {
                // 查找职位信息
                LambdaQueryWrapper<HrEmpAppointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
                appointmentQueryWrapper.eq(HrEmpAppointment::getUserCode, hrEmpBasic.getUserCode());
                List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(appointmentQueryWrapper).stream()
                        .filter(e -> e.getDutyStatus() != null).collect(Collectors.toList());

                if (!ObjectUtils.isEmpty(hrEmpAppointments)) {
                    HrEmpAppointment hrEmpAppointment = hrEmpAppointments.get(0);
                    // 如果超过一条，选择startDate最晚的
                    if (hrEmpAppointments.size() > 1) {
                        hrEmpAppointment = hrEmpAppointments.stream().reduce((a, b) -> a.getStartDate().isAfter(b.getStartDate()) ? a : b).get();
                    }
                    // 找到最新的position，里面包含了jobType
                    String jobType = hrEmpAppointment.getJobType();


                    if (!StringUtils.isNotBlank(jobType) && !jobType.contains("ERROR")) {
                        // 专业管理、市场营销职类 -> 普
                        if (jobType.startsWith("2") || jobType.startsWith("3")) {
                            return "007";
                        }

                        // 技能操作职类，直接生产操作、辅助生产操作职群 -> 计
                        if (jobType.startsWith("5-01") || jobType.startsWith("5-02")) {
                            return "008";
                        }
                        // 技能操作职类，辅助服务职群 -> 辅
                        if (jobType.startsWith("5-03")) {
                            return "009";
                        }
                        // 其他职类 -> 他
                        return "010";
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("人员分类4同步失败: {}", e.getMessage(), e);
            return hrEmpBasic.getCategory4();
        }
    }

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsg(@RequestParam String userCode) {
        List<HrEmpBasicDTO> hrEmpBasicDTOS = hrEmpBasicService.selectMsg(userCode);
        hrEmpBasicService.resolveFields(hrEmpBasicDTOS);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicDTOS);
    }

    @PostMapping("/getDetailList")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsgList(@RequestBody List<String> userCodeList) {
        List<HrEmpBasicDTO> hrEmpBasicDTOS = hrEmpBasicService.selectMsgList(userCodeList);
        hrEmpBasicService.resolveFieldsList(hrEmpBasicDTOS);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicDTOS);
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

    @PostMapping("/pageForPerSelf")
    @Operation(summary = "获取员工基本信息表列表(支持模糊查询)")
    @AuditLog(value = "获取员工基本信息表列表(模糊查询)", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<Page<HrEmpBasic>> pageForPerSelf(@Validated @RequestBody(required = false) HrEmpBasic pageReqVO,
                                                           @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                           @RequestParam(required = false, defaultValue = "10") int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpBasic() : pageReqVO;
        try {
            Page<HrEmpBasic> page = new Page<>(pageNo, pageSize);
            // 在service层实现模糊查询逻辑
            Page<HrEmpBasic> result = hrEmpBasicService.queryFuzzy(page, pageReqVO);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
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
            // 防止dept被误修改
            changeReqVO.setDept(null);
            changeReqVO.setOrgCode(null);
            HrEmpBasic byId = hrEmpBasicService.getById(changeReqVO.getId());
            boolean flag = RoleUtils.checkAnyMatch("hrsc_super_admin") || ("0".equals(byId.getIsEmployed()) && RoleUtils.checkAnyMatch("hrsc_emp_non_inc_info_mgr"));
            if (flag) {
                log.info("权限校验通过，用户拥有'公司超级管理员'或'非在职人员管理'角色，直接进行更新");
                try {
                    updateHrEmpBasic(changeReqVO).getData();
                    return new ResponseResult<>(HttpStatus.OK.value(), "直接更新成功!");
                } catch (Exception e){
                    log.warn("权限校验成功，但直接更新失败，尝试走审批流");
                }
            }
            log.warn("权限校验失败，用户缺少'公司超级管理员'或'非在职人员管理'角色");

            // 1. 获取当前数据快照
            HrEmpBasicDTO originalData = hrEmpBasicService.selectMsg(changeReqVO.getUserCode()).get(0);
            if (originalData == null) {
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "员工信息不存在");
            }

            hrEmpBasicService.resolveFields(originalData);
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
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            if(loginUser == null || loginUser.getUser() == null) {
                log.error("获取登录用户信息失败，用户为空");
                throw new IllegalArgumentException("获取登录用户信息失败，用户为空");
            }

            if(StringUtils.isBlank(loginUser.getUser().getJobNo())) {
                log.error("获取用户工号失败，工号为空");
                throw new IllegalArgumentException("获取用户工号失败，工号为空");
            }

            String deptCode = hrEmpBasicService.getDept(loginUser.getUser().getJobNo());

            if(StringUtils.isBlank(deptCode)) {
                log.error("获取用户部门失败，部门查询错误，工号:" + loginUser.getUser().getJobNo());
                throw new IllegalArgumentException("获取用户部门失败，部门查询错误");
            }

            formData.put("deptCode", deptCode);
            formData.put("type","basic");
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
            log.error("详细信息", e);
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

        // 由于附件信息不存在MongoDB中，先尝试进行查询并赋值
        HrEmpBasicDTO originalData = request.getOriginalData();
        hrEmpBasicService.resolveFiles(originalData);
        HrEmpBasicDTO changedData = request.getChangedData();
        hrEmpBasicService.resolveFiles(changedData);

        // 2. 构建返回结果（直接返回两个列表）
        Map<String, Object> result = new HashMap<>();

        // 原始数据列表（转换为Map列表或直接使用对象）
        result.put("originalData", MyUtils.convertToMapList(originalData));

        // 审批流中的数据列表
        result.put("changedData", MyUtils.convertToMapList(changedData));

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

    @GetMapping("/getDutyStatusDict")
    @AuditLog(value = "获取所有在岗状态", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "获取所有在岗状态")
    public ResponseResult<List<MapVO<String, String>>> getDutyStatusDict()  {
        try {
            // 获取包含在岗状态和人员分类8 助、派、借三个状态的字典
            List<MapVO<String, String>> result = codeInfoService.getTranslateMap("hr_emp_basic", "per_status").entrySet()
                    .stream().map(e -> new MapVO<>(e.getKey(), e.getValue())).collect(Collectors.toList());
            return new ResponseResult<>(HttpStatus.OK.value(), "获取字典成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

}
