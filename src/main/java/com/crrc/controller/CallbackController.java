package com.crrc.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.client.WorkFlowClient;
import com.crrc.entity.*;
import com.crrc.feign.WorkflowApi;
import com.crrc.mongodb.*;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.*;
import com.crrc.utils.MyUtils;
import com.crrc.utils.ProcessStatus;
import com.crrc.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author mayiyao
 */
@Slf4j
@RestController
@RequestMapping("/hrEmp")
public class CallbackController {

    /**
     * 流程模板编码，在框架界面中规定
     */

    private static final String APPROVED = "APPROVED";

    private static final String REJECTED = "REJECTED";

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private HrEmpEducationServiceImpl hrEmpEducationService;
    @Autowired
    private HrEmpFamMemberServiceImpl hrEmpFamMemberService;
    @Autowired
    private HrEmpAppointmentServiceImpl hrEmpAppointmentService;
    @Autowired
    private HrEmpPenaltiesServiceImpl hrEmpPenaltiesService;
    @Autowired
    private HrEmpCareerPathServiceImpl hrEmpCareerPathService;
    @Autowired
    private HrEmpOtherAppointmentServiceImpl hrEmpOtherAppointmentService;
    @Autowired
    private HrEmpRewardsServiceImpl hrEmpRewardsService;
    @Autowired
    private HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService;
    @Autowired
    private HrEmpResumeServiceImpl hrEmpResumeService;
    @Autowired
    private HrEmpSalaryStandardServiceImpl hrEmpSalaryStandardService;
    @Autowired
    private HrEmpSkillLevelServiceImpl hrEmpSkillLevelService;
    @Autowired
    private HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;
    @Autowired
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;
    @Autowired
    private HrEmpHealthServiceImpl hrEmpHealthService;
    @Autowired
    private HrEmpCertificateServiceImpl hrEmpCertificateService;
    @Autowired
    private HrEmpCommunicationServiceImpl hrEmpCommunicationService;
    @Autowired
    private HrEmpPaperServiceImpl hrEmpPaperService;
    @Autowired
    private HrEmpPatentServiceImpl hrEmpPatentService;
    @Autowired
    private HrEmpHonorServiceImpl hrEmpHonorService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WorkflowApi workflowApi;
    @Resource
    private WorkFlowClient workFlowClient;

    public enum UpdateType {
        // 基本信息
        BASIC,
        // 教育经历
        EDU,
        // 主要家庭成员社会关系
        FAMILY,
        // 专业职务任职资格
        PROFESSIONAL,
        // 职业技能等级
        SKILLS,
        // 职业发展通道
        CAREER_PATH,
        // 奖励情况
        REWARDS,
        // 惩处信息
        PENALTIES,
        // 个人照片
        PICTURE,
        // 论文论著
        PAPER,
        // 专利
        PATENT,
        // 荣誉
        HONOR,
    }



    /**
     * 统一回调接口
     * @param param 工作流回调参数
     * @return 处理结果
     */
    @PutMapping("/approval-callback")
    @IgnoreTenant
    public ResponseResult<Boolean> approvalCallback(@RequestBody WorkflowWriteBackParamDTO param) {
        log.info("人力资源回写接口请求参数 param ：{}",JSON.toJSONString(param));
        try {
            Map<String, Object> formDataAll;
            if (!CollectionUtils.isEmpty(param.getFormData())) {
                formDataAll = param.getFormData();
                log.info("人力资源回写接口直接获取formData formDataAll ：{}", formDataAll);
            } else {
                formDataAll = workflowApi.getFormDataByPid(param.getProcessInstanceId()).getResult();
                log.info("人力资源回写接口通过查询获取formData formDataAllQuery ：{}", formDataAll);
            }
            formDataAll.forEach((k, v) -> {
                log.info("key:" + k);
                log.info("value:" + v);
            });

            // 方法二 推荐
            String requestType = (String) formDataAll.get("type");
            log.info("人力资源回写接口approval-callback requestType2 {}", requestType);
            String bizId = MyUtils.subPk(param.getBizId());
            param.setBizId(bizId);

            switch (requestType) {
                case "basic":
                    return processBasicRequest(param);
                case "edu":
                    return processEduRequest(param);
                case "family":
                    return processFamilyRequest(param);
                case "professional":
                    return processProfessionalRequest(param);
                case "skills":
                    return processSkillsRequest(param);
                case "rewards":
                    return processRewardsRequest(param);
                case "pic":
                    return processPictureRequest(param);
                case "paper":
                    return processPaperRequest(param);
                case "patent":
                    return processPatentRequest(param);
                case "honor":
                    return processHonorRequest(param);
                default:
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "不支持的变更类型: " , false);
            }
        } catch (Exception e) {
            log.error("审批回调处理失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "处理失败", false);
        }
    }

    private ResponseResult<Boolean> processPictureRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpBasicChangeRequest request = mongoTemplate.findOne(query, HrEmpBasicChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "员工奖励情况变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpBasicPictureNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.PICTURE
        );
    }

    private void updateHrEmpBasicPictureNew(HrEmpBasicDTO changedData) {
        if (changedData == null) {
            return;
        }

        // 获取用户编码
        String userCode = changedData.getUserCode();

        // 查询现有数据
        HrEmpBasic hrEmpBasic = hrEmpBasicService.getByUserCode(userCode);

        // 更新头像
        if(hrEmpBasic != null) {
            hrEmpBasic.setProfilePicture(changedData.getProfilePicture());
            hrEmpBasicService.updateById(hrEmpBasic);
        }
    }

    /**
     * 处理员工奖励情况变更请求
     */
    private ResponseResult<Boolean> processRewardsRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpRewardsChangeRequest request = mongoTemplate.findOne(query, HrEmpRewardsChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "员工奖励情况变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpRewardsNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.REWARDS
        );
    }

    /**
     * 更新员工奖励情况信息
     */
    private void updateHrEmpRewardsNew(List<HrEmpRewards> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }

        // 获取用户编码
        String userCode = changedData.get(0).getUserCode();

        // 查询现有数据
        List<HrEmpRewards> existList = hrEmpRewardsService.selectMsg(userCode);
        Set<String> existIds = existList.stream()
                .map(HrEmpRewards::getId)
                .collect(Collectors.toSet());

        // 准备新增、更新和删除的数据
        List<HrEmpRewards> toSaveOrUpdate = new ArrayList<>();

        for (HrEmpRewards reward : changedData) {
            if (reward.getId() == null) {
                // 生成新ID
                reward.setId(IdUtil.simpleUUID());
            }
            reward.setUserCode(userCode);
            toSaveOrUpdate.add(reward);
            // 移除不需要删除的ID
            existIds.remove(reward.getId());
        }

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            hrEmpRewardsService.saveOrUpdateBatch(toSaveOrUpdate);
        }
        // 批量删除
        if (!existIds.isEmpty()) {
            hrEmpRewardsService.removeByIds(existIds);
        }
    }

    /**
     * 处理职业技能等级变更请求
     */
    private ResponseResult<Boolean> processSkillsRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpSkillsChangeRequest request = mongoTemplate.findOne(query, HrEmpSkillsChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "职业技能等级变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpSkillsNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.SKILLS
        );
    }

    /**
     * 更新职业技能等级信息
     */
    private void updateHrEmpSkillsNew(List<HrEmpSkillLevel> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }

        // 获取用户编码
        String userCode = changedData.get(0).getUserCode();

        // 查询现有数据
        List<HrEmpSkillLevel> existList = hrEmpSkillLevelService.selectMsg(userCode);
        Set<String> existIds = existList.stream()
                .map(HrEmpSkillLevel::getId)
                .collect(Collectors.toSet());

        // 准备新增、更新和删除的数据
        List<HrEmpSkillLevel> toSaveOrUpdate = new ArrayList<>();

        for (HrEmpSkillLevel skill : changedData) {
            if (skill.getId() == null) {
                // 生成新ID
                skill.setId(IdUtil.simpleUUID());
            }
            skill.setUserCode(userCode);
            toSaveOrUpdate.add(skill);
            // 移除不需要删除的ID
            existIds.remove(skill.getId());
        }

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            hrEmpSkillLevelService.saveOrUpdateBatch(toSaveOrUpdate);
        }
        // 批量删除
        if (!existIds.isEmpty()) {
            hrEmpSkillLevelService.removeByIds(existIds);
        }
    }


    /**
     * 处理专业职务任职资格变更请求
     */
    private ResponseResult<Boolean> processProfessionalRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpProfessionalChangeRequest request = mongoTemplate.findOne(query, HrEmpProfessionalChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "专业职务任职资格变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpProfessionalNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.PROFESSIONAL
        );
    }

    /**
     * 更新专业职务任职资格信息
     */
    private void updateHrEmpProfessionalNew(List<HrEmpProfessionalQualification> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }

        // 获取用户编码
        String userCode = changedData.get(0).getUserCode();

        // 查询现有数据
        List<HrEmpProfessionalQualification> existList = hrEmpProfessionalQualificationService.selectMsg(userCode);
        Set<String> existIds = existList.stream()
                .map(HrEmpProfessionalQualification::getId)
                .collect(Collectors.toSet());

        // 准备新增、更新和删除的数据
        List<HrEmpProfessionalQualification> toSaveOrUpdate = new ArrayList<>();

        for (HrEmpProfessionalQualification qualification : changedData) {
            if (qualification.getId() == null) {
                // 生成新ID
                qualification.setId(IdUtil.simpleUUID());
            }
            qualification.setUserCode(userCode);
            toSaveOrUpdate.add(qualification);
            // 移除不需要删除的ID
            existIds.remove(qualification.getId());
        }

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            hrEmpProfessionalQualificationService.saveOrUpdateBatch(toSaveOrUpdate);
        }
        // 批量删除
        if (!existIds.isEmpty()) {
            hrEmpProfessionalQualificationService.removeByIds(existIds);
        }
    }

    /**
     * 处理基本信息变更请求
     */
    private ResponseResult<Boolean> processBasicRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpBasicChangeRequest request = mongoTemplate.findOne(query, HrEmpBasicChangeRequest.class);
        log.info("查询请求数据成功: " + request);
        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "基本信息变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getChangedData().getUserCode(),
                request::setStatus,
                () -> updateHrEmpBasicNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.BASIC
        );
    }

    /**
     * 处理教育信息变更请求
     */
    private ResponseResult<Boolean> processEduRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));

        log.info("流程实例ID：{}", param.getBizId());
        HrEmpEduChangeRequest request = mongoTemplate.findOne(query, HrEmpEduChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "教育信息变更申请不存在", false);
        }

        log.info("等待更新的教育经历变更数据：{}", JSON.toJSONString(request.getChangedData(), SerializerFeature.PrettyFormat));

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpEduNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.EDU
        );
    }

    /**
     * 处理论文论著变更请求
     */
    private ResponseResult<Boolean> processPaperRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));

        log.info("流程实例ID：{}", param.getBizId());
        HrEmpPaperChangeRequest request = mongoTemplate.findOne(query, HrEmpPaperChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "论文论著变更申请不存在", false);
        }

        log.info("等待更新的论文论著变更数据：{}", JSON.toJSONString(request.getChangedData(), SerializerFeature.PrettyFormat));

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpPaperNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.PAPER
        );
    }

    /**
     * 处理专利变更请求
     */
    private ResponseResult<Boolean> processPatentRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));

        log.info("流程实例ID：{}", param.getBizId());
        HrEmpPatentChangeRequest request = mongoTemplate.findOne(query, HrEmpPatentChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "专利变更申请不存在", false);
        }

        log.info("等待更新的专利变更数据：{}", JSON.toJSONString(request.getChangedData(), SerializerFeature.PrettyFormat));

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpPatentNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.PATENT
        );
    }

    /**
     * 处理荣誉变更请求
     */
    private ResponseResult<Boolean> processHonorRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));

        log.info("流程实例ID：{}", param.getBizId());
        HrEmpHonorChangeRequest request = mongoTemplate.findOne(query, HrEmpHonorChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "荣誉变更申请不存在", false);
        }

        log.info("等待更新的荣誉变更数据：{}", JSON.toJSONString(request.getChangedData(), SerializerFeature.PrettyFormat));

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpHonorNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.HONOR
        );
    }

    /**
     * 处理家庭主要成员的变更请求
     */
    private ResponseResult<Boolean> processFamilyRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpFamMemberChangeRequest request = mongoTemplate.findOne(query, HrEmpFamMemberChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "家庭成员变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpFamMemberNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus(),
                UpdateType.FAMILY
        );
    }

    /**
     * 更新家庭成员信息
     */
    private void updateHrEmpFamMemberNew(List<HrEmpFamMember> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }

        // 获取用户编码（假设所有家庭成员记录都有相同的userCode）
        String userCode = changedData.get(0).getUserCode();

        // 新增待删除数据列表
        List<String> deleteIdList = new ArrayList<>();

        // 准备新增、更新的数据
        List<HrEmpFamMember> toSaveOrUpdate = new ArrayList<>();

        // 判断传入数据里面有无紧急联系人
        boolean isEmergency = true;
        int emergencyNum = 0;
        for (HrEmpFamMember hrEmpFamMember : changedData) {
            // 检查删除标记，有就删除
            if(!hrEmpFamMember.getMarkList().isEmpty() && ("$DELETE$").equals(hrEmpFamMember.getMarkList().get(0))) {
                deleteIdList.add(hrEmpFamMember.getId());
                continue;
            }

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
                throw new RuntimeException("紧急联系人数量不唯一！");
            }
            hrEmpFamMember.setUserCode(userCode);
            toSaveOrUpdate.add(hrEmpFamMember);
        }
        // 没有紧急联系人,且家庭成员列表不为空时
        if(isEmergency && !changedData.isEmpty()) {
            throw new RuntimeException("录入员工主要家庭成员中未设置紧急联系人！");
        }

        // 批量保存或更新
        hrEmpFamMemberService.saveOrUpdateBatch(toSaveOrUpdate);

        // 批量删除
        if (!deleteIdList.isEmpty()) {
            hrEmpFamMemberService.removeByIds(deleteIdList);
        }
    }
    /**
     * 更新论文论著信息
     */
    private void updateHrEmpPaperNew(List<HrEmpPaper> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }

        // 获取用户编码
        String userCode = changedData.get(0).getUserCode();

        // 准备新增、更新的数据
        List<HrEmpPaper> toSaveOrUpdate = new ArrayList<>();

        // 新增待删除数据列表
        List<String> deleteIdList = new ArrayList<>();

        for (HrEmpPaper hrEmpPaper : changedData) {
            // 检查删除标记，有就删除
            if(!hrEmpPaper.getMarkList().isEmpty() && ("$DELETE$").equals(hrEmpPaper.getMarkList().get(0))) {
                deleteIdList.add(hrEmpPaper.getId());
                continue;
            }

            if (hrEmpPaper.getId() == null) {
                // 生成新ID
                hrEmpPaper.setId(IdUtil.simpleUUID());
            }

            hrEmpPaper.setUserCode(userCode);
            toSaveOrUpdate.add(hrEmpPaper);
        }

        // 批量保存或更新
        hrEmpPaperService.saveOrUpdateBatch(toSaveOrUpdate);

        // 批量删除
        if (!deleteIdList.isEmpty()) {
            hrEmpPaperService.removeByIds(deleteIdList);
        }
    }

    /**
     * 更新专利信息
     */
    private void updateHrEmpPatentNew(List<HrEmpPatent> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }
        // 获取用户编码
        String userCode = changedData.get(0).getUserCode();

        // 准备新增、更新的数据
        List<HrEmpPatent> toSaveOrUpdate = new ArrayList<>();

        // 新增待删除数据列表
        List<String> deleteIdList = new ArrayList<>();

        for (HrEmpPatent hrEmpPatent : changedData) {
            // 检查删除标记，有就删除
            if(!hrEmpPatent.getMarkList().isEmpty() && ("$DELETE$").equals(hrEmpPatent.getMarkList().get(0))) {
                deleteIdList.add(hrEmpPatent.getId());
                continue;
            }

            if (hrEmpPatent.getId() == null) {
                // 生成新ID
                hrEmpPatent.setId(IdUtil.simpleUUID());
            }

            hrEmpPatent.setUserCode(userCode);
            toSaveOrUpdate.add(hrEmpPatent);
        }

        // 批量保存或更新
        hrEmpPatentService.saveOrUpdateBatch(toSaveOrUpdate);

        // 批量删除
        if (!deleteIdList.isEmpty()) {
            hrEmpPatentService.removeByIds(deleteIdList);
        }
    }

    /**
     * 更新荣誉信息
     */
    private void updateHrEmpHonorNew(List<HrEmpHonor> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }
        // todo 方法变更，现在为一对一更新
        // 先看是不是删除
        HrEmpHonor hrEmpHonor = changedData.get(0);
        if (StringUtils.isNotBlank(hrEmpHonor.getId()) && StringUtils.isBlank(hrEmpHonor.getResponsibleUnit())) {
            // id不为空，归口单位为空，删除
            hrEmpHonorService.removeById(hrEmpHonor);
        } else {
            // id不为空，归口单位不为空，更新
            // id为空，新增
            // 准备新增、更新的数据
            if (hrEmpHonor.getId() == null) {
                // 生成新ID
                hrEmpHonor.setId(IdUtil.simpleUUID());
            }
            hrEmpHonorService.saveOrUpdate(hrEmpHonor);
        }
    }

    /**
     * 通用请求处理逻辑
     * @param request mangoDB存储的PO对象
     * @param currentStatus 当前审批状态，PENDING/APPROVED/REJECTED
     * @param userCode 用户档案号
     * @param statusSetter 设置数据审批状态的回调函数
     * @param updateAction 保存或更新数据PO的回调方法
     * @param statusChecker 判断status状态，会向回调函数传入ProcessStatus
     * @param processInstanceStatus 流程实例状态
     * @param updateType 根据updateType的类型来解锁行数据
     */
    private <T> ResponseResult<Boolean> processRequest(
            T request,
            String currentStatus,
            String userCode,
            Consumer<String> statusSetter,
            Runnable updateAction,
            java.util.function.Predicate<String> statusChecker,
            Integer processInstanceStatus,
            UpdateType updateType
    ) {
        log.info("进入通用回调申请方法成功processInstanceStatus： " + processInstanceStatus);
        String isUpdatingValue = "1";

        if (processInstanceStatus.equals(ProcessStatus.COMPLETED) ) {
            // 审批通过
            if (statusChecker.test(currentStatus)) {
                return new ResponseResult<>(HttpStatus.OK.value(), "已处理过该申请", true);
            }

            updateAction.run();
            statusSetter.accept(APPROVED);
            isUpdatingValue = "0";

        } else if (processInstanceStatus.equals(ProcessStatus.REJECTED) || processInstanceStatus.equals(ProcessStatus.RESET)) {
            // 审批拒绝
            statusSetter.accept(REJECTED);
            isUpdatingValue = "0";
        }
        mongoTemplate.save(request);
        // 执行更新状态方法
        log.info("执行更新状态方法: " +  isUpdatingValue);
        updateIsUpdating(isUpdatingValue, userCode, updateType);


        return new ResponseResult<>(HttpStatus.OK.value(), "处理成功", true);
    }

    /**
     * 更新基本信息
     */
    private void updateHrEmpBasicNew(HrEmpBasicDTO updateReqVO) {
        String userCode = updateReqVO.getUserCode();

        // 更新健康信息
        HrEmpHealth health = hrEmpHealthService.selectByUserCode(userCode);
        if (health == null) {
            health = new HrEmpHealth();
            health.setUserCode(userCode);
            health.setId(IdUtil.simpleUUID());
        }
        health.setHealthStatus(updateReqVO.getHealthStatus());
        hrEmpHealthService.saveOrUpdate(health);

        // 更新户口信息
        HrEmpHouseHold household = hrEmpHouseholdService.selectByUserCode(userCode);
        if (household == null) {
            household = new HrEmpHouseHold();
            household.setUserCode(userCode);
            household.setId(IdUtil.simpleUUID());
        }
        household.setHouseHoldLocation(updateReqVO.getHouseHoldLocation());
        household.setHouseHoldLocationDetail(updateReqVO.getHouseHoldLocationDetail());
        household.setHouseHoldType(updateReqVO.getHouseHoldType());
        hrEmpHouseholdService.saveOrUpdate(household);

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

        // 更新基本信息
        if (updateReqVO.getId() == null) {
            updateReqVO.setId(IdUtil.simpleUUID());
            hrEmpBasicService.save(updateReqVO);
        }
        hrEmpBasicService.updateById(updateReqVO);
    }

    /**
     * 更新教育信息
     */
    private void updateHrEmpEduNew(List<HrEmpEducation> changedData) {
        if (changedData == null || changedData.isEmpty()) {
            return;
        }
        // 获取用户编码（假设所有教育记录都有相同的userCode）
        String userCode = changedData.get(0).getUserCode();

        // 新增待删除数据列表
        List<String> deleteIdList = new ArrayList<>();

        // 准备新增、更新和删除的数据
        List<HrEmpEducation> toSaveOrUpdate = new ArrayList<>();

        // 判断传入数据里面有无最高学历
        boolean isHighestEdu = true;
        int isHighestNum = 0;
        for (HrEmpEducation hrEmpEducation : changedData) {
            // 检查删除标记，有就删除
            if(!hrEmpEducation.getMarkList().isEmpty() && ("$DELETE$").equals(hrEmpEducation.getMarkList().get(0))) {
                deleteIdList.add(hrEmpEducation.getId());
                continue;
            }

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
                throw new RuntimeException("最高学历数量不唯一！");
            }
            hrEmpEducation.setUserCode(userCode);
            toSaveOrUpdate.add(hrEmpEducation);
        }
        // 没有最高学历,且教育信息列表不为空时
        if(isHighestEdu && !changedData.isEmpty()) {
            throw new RuntimeException("录入教育经历中未设置最高学历！");
        }

        // 批量保存或更新
        hrEmpEducationService.saveOrUpdateBatch(toSaveOrUpdate);

        // 批量删除
        if (!deleteIdList.isEmpty()) {
            hrEmpEducationService.removeByIds(deleteIdList);
        }
    }

    /**
     * 更新是否在更新状态
     * @param isUpdating 更新状态值
     * @param userCode 用户编码
     * @param updateType 更新类型
     */
    private void updateIsUpdating(String isUpdating, String userCode, UpdateType updateType) {
        switch (updateType) {
            case BASIC:
            case PICTURE:
                updateBasicIsUpdating(isUpdating, userCode);
                break;
            case EDU:
                updateEduIsUpdating(isUpdating, userCode);
                break;
            case FAMILY:
                updateFamilyIsUpdating(isUpdating, userCode);
                break;
            case SKILLS:
                updateSkillsIsUpdating(isUpdating, userCode);
                break;
            case REWARDS:
                updateRewardsIsUpdating(isUpdating, userCode);
                break;
            case PENALTIES:
                updatePenaltiesIsUpdating(isUpdating, userCode);
                break;
            case CAREER_PATH:
                updateCareerPathIsUpdating(isUpdating, userCode);
                break;
            case PROFESSIONAL:
                updateProfessionalIsUpdating(isUpdating, userCode);
                break;
            case PAPER:
                updatePaperIsUpdating(isUpdating, userCode);
                break;
            case PATENT:
                updatePatentIsUpdating(isUpdating, userCode);
                break;
            case HONOR:
                updateHonorIsUpdating(isUpdating, userCode);
                break;
            default:
                log.warn("未知的更新类型: {}", updateType);
                break;
        }
    }

    private void updateBasicIsUpdating(String isUpdating, String userCode) {
        List<HrEmpBasicDTO> basics = hrEmpBasicService.selectMsg(userCode);
        if (!basics.isEmpty()) {
            HrEmpBasic temp = basics.get(0);
            temp.setIsUpdating(isUpdating);
            // 防止dept被误修改
            temp.setDept(null);
            temp.setOrgCode(null);
            hrEmpBasicService.updateById(temp);
        }
    }

    private void updateEduIsUpdating(String isUpdating, String userCode) {
        // 教育信息的更新状态逻辑
        List<HrEmpEducation> edus = hrEmpEducationService.selectMsg(userCode);
        for(HrEmpEducation temp : edus) {
            temp.setIsUpdating(isUpdating);
            hrEmpEducationService.updateById(temp);
        }
    }

    private void updateFamilyIsUpdating(String isUpdating, String userCode) {
        // 家庭成员信息的更新状态逻辑
        List<HrEmpFamMember> familyMembers = hrEmpFamMemberService.selectMsg(userCode);
        for(HrEmpFamMember temp : familyMembers) {
            temp.setIsUpdating(isUpdating);
            hrEmpFamMemberService.updateById(temp);
        }
    }

    private void updateSkillsIsUpdating(String isUpdating, String userCode) {
        List<HrEmpSkillLevel> skills = hrEmpSkillLevelService.selectMsg(userCode);
        for(HrEmpSkillLevel temp : skills) {
            temp.setIsUpdating(isUpdating);
            hrEmpSkillLevelService.updateById(temp);
        }
    }

    private void updateRewardsIsUpdating(String isUpdating, String userCode) {
        List<HrEmpRewards> rewards = hrEmpRewardsService.selectMsg(userCode);
        for(HrEmpRewards temp : rewards) {
            temp.setIsUpdating(isUpdating);
            hrEmpRewardsService.updateById(temp);
        }
    }

    private void updatePenaltiesIsUpdating(String isUpdating, String userCode) {
        List<HrEmpPenalties> penalties = hrEmpPenaltiesService.selectMsg(userCode);
        for(HrEmpPenalties temp : penalties) {
            temp.setIsUpdating(isUpdating);
            hrEmpPenaltiesService.updateById(temp);
        }
    }

    private void updatePaperIsUpdating(String isUpdating, String userCode) {
        List<HrEmpPaper> paper = hrEmpPaperService.selectMsg(userCode);
        for(HrEmpPaper temp : paper) {
            temp.setIsUpdating(isUpdating);
            hrEmpPaperService.updateById(temp);
        }
    }

    private void updatePatentIsUpdating(String isUpdating, String userCode) {
        List<HrEmpPatent> patent = hrEmpPatentService.selectMsg(userCode);
        for(HrEmpPatent temp : patent) {
            temp.setIsUpdating(isUpdating);
            hrEmpPatentService.updateById(temp);
        }
    }

    private void updateHonorIsUpdating(String isUpdating, String userCode) {
        List<HrEmpHonor> honor = hrEmpHonorService.selectMsg(userCode);
        for(HrEmpHonor temp : honor) {
            temp.setIsUpdating(isUpdating);
            hrEmpHonorService.updateById(temp);
        }
    }

    private void updateCareerPathIsUpdating(String isUpdating, String userCode) {
        List<HrEmpCareerPath> careerPaths = hrEmpCareerPathService.selectMsg(userCode);
        for(HrEmpCareerPath temp : careerPaths) {
            temp.setIsUpdating(isUpdating);
            hrEmpCareerPathService.updateById(temp);
        }
    }

    private void updateProfessionalIsUpdating(String isUpdating, String userCode) {
        List<HrEmpProfessionalQualification> professionals = hrEmpProfessionalQualificationService.selectMsg(userCode);
        for(HrEmpProfessionalQualification temp : professionals) {
            temp.setIsUpdating(isUpdating);
            hrEmpProfessionalQualificationService.updateById(temp);
        }
    }
}
