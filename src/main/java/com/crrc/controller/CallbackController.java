package com.crrc.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.api.WkWorkflowApi;
import com.crrc.client.WorkFlowClient;
import com.crrc.common.ResultData;
import com.crrc.mongodb.HrEmpBasicChangeRequest;
import com.crrc.mongodb.HrEmpEduChangeRequest;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crrc.entity.*;
import com.crrc.pojo.dto.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;
    @Autowired
    private HrEmpHealthServiceImpl hrEmpHealthService;
    @Autowired
    private HrEmpCertificateServiceImpl hrEmpCertificateService;
    @Autowired
    private HrEmpCommunicationServiceImpl hrEmpCommunicationService;
    @Autowired
    private MongoTemplate mongoTemplate;
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
            log.info("基本信息approval-callback 请求参数: {}", JSON.toJSONString(param));
            String processInstanceId = param.getProcessInstanceId();
            if (ObjectUtils.isEmpty(processInstanceId)) {
                return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "processInstanceId不能为空");
            }
            // 获取表单数据
            ResultData<Map<String, Object>> fromDataByProcessInstanceId = workFlowClient.getFromDataByProcessInstanceId(processInstanceId);
            log.info("基本信息approval-callback fromDataByProcessInstanceId: {} processInstanceId {}", fromDataByProcessInstanceId, processInstanceId);
            if (ObjectUtils.isEmpty(fromDataByProcessInstanceId)) {
                return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "获取到的表单数据为空！");
            }
            Map<String, Object> formDataResult = fromDataByProcessInstanceId.getResult();
            log.info("基本信息approval-callback formDataResult: {}", formDataResult);



            return processBasicRequest(param);
//            switch (requestType) {
//                case "basic":
//                    return processBasicRequest(param);
//                case "edu":
//                    return processEduRequest(param);
//                default:
//                    return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "不支持的变更类型: " , false);
//            }
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
        updateIsUpdating(isUpdatingValue, userCode);

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
     * 更新教育信息（示例方法）
     */
    private void updateHrEmpEduNew(List<HrEmpEducation> changedData) {
        // 实际实现应根据业务需求编写
        changedData.forEach(edu -> {
            if (edu.getId() == null) {
                edu.setId(IdUtil.simpleUUID());
                hrEmpEducationService.save(edu);
            } else {
                hrEmpEducationService.updateById(edu);
            }
        });
    }

    /**
     * 更新是否在更新状态
     */
    private void updateIsUpdating(String isUpdating, String userCode) {
        List<HrEmpBasicDTO> basics = hrEmpBasicService.selectMsg(userCode);
        if (!basics.isEmpty()) {
            HrEmpBasic temp = basics.get(0);
            temp.setIsUpdating(isUpdating);
            hrEmpBasicService.updateById(temp);
        }
    }

}
