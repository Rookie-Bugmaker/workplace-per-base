package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpOtherAppointment;
import com.crrc.entity.HrEmpResume;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.KingDiePersonalOrderDTO;
import com.crrc.service.impl.*;
import com.crrc.utils.UpdateWrapperUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 免登录方法集合
 */
@Slf4j
@RestController
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
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

    @PostMapping("/receiveKingDieMsg")
    @Operation(summary = "接收金蝶传递人事令信息")
    @AuditLog(value = "接收金蝶传递人事令信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Integer> receiveKingDieMsg(@RequestBody List<KingDiePersonalOrderDTO> dtoList) {
        // @TODO 免登录接口需要手动写入租户Id，测试环境438，生产32，记得切换
        MultiTenantHandler.tenantId.set("438");
        int updateFlag = 0;
        for (KingDiePersonalOrderDTO dto : dtoList) {
            try {
                //任职信息
                if (dto.getHrEmpAppointments() != null && dto.getHrEmpAppointments().size() != 0){
                    for (HrEmpAppointment appointment : dto.getHrEmpAppointments()){
                        if (appointment.getUserCode() != null) {
                            appointment.setId(IdUtil.simpleUUID());
                            boolean saveResult = hrEmpAppointmentService.save(appointment);
                            if (!saveResult) {
                                log.error("保存任职信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存任职信息失败!");
                            }
                        }
                    }
                    //更新之前任职信息的结束时间
                    List<HrEmpAppointment> undoneList = hrEmpAppointmentService.selectUndoneApp(dto.getHrEmpAppointments().get(0).getUserCode());
                    for (HrEmpAppointment temp : undoneList){
                        temp.setEndDate(dto.getHrEmpAppointments().get(0).getStartDate());
                    }
                    hrEmpAppointmentService.updateBatchById(undoneList);
                }

                // 员工基本信息表 - 按工号更新
                if (dto.getHrEmpBasic() != null) {
                    HrEmpBasic basic = dto.getHrEmpBasic();
                    if (basic.getUserCode() == null) {
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "工号不能为空!");
                    }

                    UpdateWrapper<HrEmpBasic> updateWrapper = UpdateWrapperUtils
                            .buildNonNullUpdateWrapper(basic, "id", "userCode")
                            .eq("user_code", basic.getUserCode());

                    boolean updateResult = hrEmpBasicService.update(updateWrapper);
                    if (!updateResult) {
                        log.error("更新基本信息失败，工号：{}", basic.getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新基本信息失败!");
                    }
                }

                //职业发展通道
                if (dto.getHrEmpCareerPath() != null && dto.getHrEmpCareerPath().getUserCode() != null) {
                    dto.getHrEmpCareerPath().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpCareerPathService.save(dto.getHrEmpCareerPath());
                    if (!saveResult) {
                        log.error("保存职业发展通道失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存职业发展通道失败!");
                    }
                }

                //其他任职信息
                if (dto.getHrEmpOtherAppointments() != null && dto.getHrEmpOtherAppointments().size() != 0){
                    for (HrEmpOtherAppointment otherApp : dto.getHrEmpOtherAppointments()){
                        if (otherApp.getUserCode() != null) {
                            otherApp.setId(IdUtil.simpleUUID());
                            boolean saveResult = hrEmpOtherAppointmentService.save(otherApp);
                            if (!saveResult) {
                                log.error("保存其他任职信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存其他任职信息失败!");
                            }
                        }
                    }
                    //更新之前任职信息的结束时间
                    List<HrEmpOtherAppointment> undoneList = hrEmpOtherAppointmentService.selectUndoneApp(dto.getHrEmpOtherAppointments().get(0).getUserCode());
                    for (HrEmpOtherAppointment temp : undoneList){
                        temp.setEndDate(dto.getHrEmpOtherAppointments().get(0).getStartDate());
                    }
                    hrEmpOtherAppointmentService.updateBatchById(undoneList);
                }


                //员工惩处信息表
                if (dto.getHrEmpPenalties() != null && dto.getHrEmpPenalties().getUserCode() != null) {
                    dto.getHrEmpPenalties().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpPenaltiesService.save(dto.getHrEmpPenalties());
                    if (!saveResult) {
                        log.error("保存惩处信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存惩处信息失败!");
                    }
                }

                //员工专业职务任职资格表
                if (dto.getHrEmpProfessionalQualification() != null && dto.getHrEmpProfessionalQualification().getUserCode() != null) {
                    dto.getHrEmpProfessionalQualification().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpProfessionalQualificationService.save(dto.getHrEmpProfessionalQualification());
                    if (!saveResult) {
                        log.error("保存专业职务任职资格失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存专业职务任职资格失败!");
                    }
                }

                //员工履历信息表
                if (dto.getHrEmpResumes() != null && dto.getHrEmpResumes().size() != 0){
                    for (HrEmpResume resume : dto.getHrEmpResumes()){
                        if (resume.getUserCode() != null) {
                            resume.setId(IdUtil.simpleUUID());
                            boolean saveResult = hrEmpResumeService.save(resume);
                            if (!saveResult) {
                                log.error("保存履历信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存履历信息失败!");
                            }
                        }
                    }
                }

                //员工奖励情况表
                if (dto.getHrEmpRewards() != null && dto.getHrEmpRewards().getUserCode() != null) {
                    dto.getHrEmpRewards().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpRewardsService.save(dto.getHrEmpRewards());
                    if (!saveResult) {
                        log.error("保存奖励情况失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存奖励情况失败!");
                    }
                }

                //薪资标准
                if (dto.getHrEmpSalaryStandard() != null && dto.getHrEmpSalaryStandard().getUserCode() != null) {
                    dto.getHrEmpSalaryStandard().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpSalaryStandardService.save(dto.getHrEmpSalaryStandard());
                    if (!saveResult) {
                        log.error("保存薪资标准失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存薪资标准失败!");
                    }
                }

                //职业技能等级
                if (dto.getHrEmpSkillLevel() != null && dto.getHrEmpSkillLevel().getUserCode() != null) {
                    dto.getHrEmpSkillLevel().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpSkillLevelService.save(dto.getHrEmpSkillLevel());
                    if (!saveResult) {
                        log.error("保存职业技能等级失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存职业技能等级失败!");
                    }
                }
                updateFlag++;
            } catch (Exception e) {
                log.error("处理人事令信息异常，工号：{}，异常信息：{}",
                        dto.getHrEmpBasic() != null ? dto.getHrEmpBasic().getUserCode() : "未知",
                        e.getMessage(), e);
                throw new RuntimeException("处理人事令信息异常：" + e.getMessage());
            }
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "人事令下达成功，信息已变更！ 变更数量： " + updateFlag);
    }
}
