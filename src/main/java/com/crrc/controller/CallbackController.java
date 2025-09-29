package com.crrc.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.client.WorkFlowClient;
import com.crrc.feign.WorkflowApi;
import com.crrc.mongodb.HrEmpBasicChangeRequest;
import com.crrc.mongodb.HrEmpEduChangeRequest;
import com.crrc.mongodb.HrEmpFamMemberChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.*;
import com.crrc.utils.MyUtils;
import com.crrc.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crrc.entity.*;
import com.crrc.pojo.dto.*;

import javax.annotation.Resource;
import java.util.*;
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
    private MongoTemplate mongoTemplate;
    @Autowired
    private WorkflowApi workflowApi;
    @Resource
    private WorkFlowClient workFlowClient;



    /**
     * 统一回调接口
     * @param param 工作流回调参数
     * @return 处理结果
     */
    @PutMapping("/approval-callback")
    @IgnoreTenant
    public ResponseResult<Boolean> approvalCallback(@RequestBody WorkflowWriteBackParamDTO param) {
        try {
            log.info("流程状态：" + param.getProcessInstanceStatus());
            Map<String, Object> formDataAll = param.getFormData() != null ? param.getFormData() : workflowApi.getFormDataByPid(param.getProcessInstanceId()).getResult();
            log.info("基本信息approval-callback 请求参数: {}", JSON.toJSONString(param));
            log.info("基本信息approval-callback formDataAll {}", formDataAll);
//            // 方法一 不好使
//            JSONObject jsonObj = new JSONObject(formDataAll);
//            String requestType = jsonObj.getJSONObject("formData").getString("type");
//            log.info("基本信息方法一approval-callback requestType {}", requestType);

            formDataAll.forEach((k, v) -> {
                log.info("key:" + k);
                log.info("value:" + v);
            });

            // 方法二 推荐
            String requestType2 = Optional.ofNullable(formDataAll)
                    .map(formData -> (String) formData.get("type"))
                    .orElse("default_value");
            log.info("基本信息方法二approval-callback requestType2 {}", requestType2);
            String bizId = MyUtils.subPk(param.getBizId());
            param.setBizId(bizId);

            switch (requestType2) {
                case "basic":
                    return processBasicRequest(param);
                case "edu":
                    return processEduRequest(param);
                case "family":
                    return processFamilyRequest(param);
//                case "professional":
//                    return processProfessionalRequest(param);
//                case "skills":
//                    return processSkillsRequest(param);
//                case "careerPath":
//                    return processCareerPathRequest(param);
//                case "rewards":
//                    return processRewardsRequest(param);
//                case "penalties":
//                    return processPenaltiesRequest(param);
                default:
                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "不支持的变更类型: " , false);
            }
        } catch (Exception e) {
            log.error("审批回调处理失败: {}", e.getMessage(), e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "处理失败", false);
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
                param.getProcessInstanceStatus()
        );
    }

    /**
     * 处理教育信息变更请求
     */
    private ResponseResult<Boolean> processEduRequest(WorkflowWriteBackParamDTO param) {
        Query query = new Query(Criteria.where("_id").is(param.getBizId()));
        HrEmpEduChangeRequest request = mongoTemplate.findOne(query, HrEmpEduChangeRequest.class);

        if (request == null) {
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "教育信息变更申请不存在", false);
        }

        return processRequest(
                request,
                request.getStatus(),
                request.getUserCode(),
                request::setStatus,
                () -> updateHrEmpEduNew(request.getChangedData()),
                APPROVED::equals,
                param.getProcessInstanceStatus()
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
                param.getProcessInstanceStatus()
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

        // 查询现有数据
        List<HrEmpFamMember> existList = hrEmpFamMemberService.selectMsg(userCode);
        Set<String> existIds = existList.stream()
                .map(HrEmpFamMember::getId)
                .collect(Collectors.toSet());

        // 准备新增、更新和删除的数据
        List<HrEmpFamMember> toSaveOrUpdate = new ArrayList<>();

        // 判断传入数据里面有无紧急联系人
        boolean isEmergency = true;
        int emergencyNum = 0;
        for (HrEmpFamMember hrEmpFamMember : changedData) {
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
            // 移除不需要删除的ID
            existIds.remove(hrEmpFamMember.getId());
        }
        // 没有紧急联系人,且家庭成员列表不为空时
        if(isEmergency && !changedData.isEmpty()) {
            throw new RuntimeException("录入员工主要家庭成员中未设置紧急联系人！");
        }

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            hrEmpFamMemberService.saveOrUpdateBatch(toSaveOrUpdate);
        }
        // 批量删除
        if (!existIds.isEmpty()) {
            hrEmpFamMemberService.removeByIds(existIds);
        }
    }


    /**
     * 通用请求处理逻辑
     */
    private <T> ResponseResult<Boolean> processRequest(
            T request,
            String currentStatus,
            String userCode,
            Consumer<String> statusSetter,
            Runnable updateAction,
            java.util.function.Predicate<String> statusChecker,
            Integer processInstanceStatus
    ) {
        log.info("进入通用回调申请方法成功");
        String isUpdatingValue = "1";

        if (processInstanceStatus == 2) {
            // 审批通过
            if (statusChecker.test(currentStatus)) {
                return new ResponseResult<>(HttpStatus.OK.value(), "已处理过该申请", true);
            }

            updateAction.run();
            statusSetter.accept(APPROVED);
            isUpdatingValue = "0";

        } else if (processInstanceStatus == 3) {
            // 审批拒绝
            statusSetter.accept(REJECTED);
            isUpdatingValue = "0";
        }
        mongoTemplate.save(request);
        // 执行更新状态方法
        log.info("执行更新状态方法");
        updateIsUpdating(isUpdatingValue, userCode);

        return new ResponseResult<>(HttpStatus.OK.value(), "处理成功", true);
    }

    /**
     * 更新基本信息
     */
    private void updateHrEmpBasicNew(HrEmpBasicDTO updateReqVO) {
        log.info("updateReqVO HealthStatus:" + updateReqVO.getHealthStatus());
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
        log.info("updateHrEmpBasicNew finished");
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

        // 查询现有数据
        List<HrEmpEducation> existList = hrEmpEducationService.selectMsg(userCode);
        Set<String> existIds = existList.stream()
                .map(HrEmpEducation::getId)
                .collect(Collectors.toSet());

        // 准备新增、更新和删除的数据
        List<HrEmpEducation> toSaveOrUpdate = new ArrayList<>();

        // 判断传入数据里面有无最高学历
        boolean isHighestEdu = true;
        int isHighestNum = 0;
        for (HrEmpEducation hrEmpEducation : changedData) {
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
            // 移除不需要删除的ID
            existIds.remove(hrEmpEducation.getId());
        }
        // 没有最高学历,且教育信息列表不为空时
        if(isHighestEdu && !changedData.isEmpty()) {
            throw new RuntimeException("录入教育经历中未设置最高学历！");
        }

        // 批量保存或更新
        if (!toSaveOrUpdate.isEmpty()) {
            hrEmpEducationService.saveOrUpdateBatch(toSaveOrUpdate);
        }
        // 批量删除
        if (!existIds.isEmpty()) {
            hrEmpEducationService.removeByIds(existIds);
        }
    }

    /**
     * 更新是否在更新状态
     */
    private void updateIsUpdating(String isUpdating, String userCode) {
        List<HrEmpBasicDTO> basics = hrEmpBasicService.selectMsg(userCode);
        log.info("basicList的大小：" + basics.size());
        if (!basics.isEmpty()) {
            HrEmpBasic temp = basics.get(0);
            temp.setIsUpdating(isUpdating);
            hrEmpBasicService.updateById(temp);
        }
    }

}
