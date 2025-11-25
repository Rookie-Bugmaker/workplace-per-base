package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.entity.*;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.KingDiePersonalOrderDTO;
import com.crrc.service.CategoryRecordService;
import com.crrc.service.impl.*;
import com.crrc.utils.MinIOUtils;
import com.crrc.utils.UpdateWrapperUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;
    @Autowired
    private HrEmpBasicController hrEmpBasicController;
    @Autowired
    private CategoryRecordService categoryRecordService;
    @Autowired
    private MinIOUtils minIOUtils;

    @PostMapping("/receiveKingDieMsg")
    @Operation(summary = "接收金蝶传递人事令信息")
    @AuditLog(value = "接收金蝶传递人事令信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Integer> receiveKingDieMsg(@RequestBody List<KingDiePersonalOrderDTO> dtoList) {

        // @TODO 免登录接口需要手动写入租户Id，测试环境438，生产32，记得切换
        int updateFlag = 0;
        for (KingDiePersonalOrderDTO dto : dtoList) {
            try {
                MultiTenantHandler.tenantId.set("438");
                // 员工基本信息表 - 按工号更新或新增，先查询，如果没有该档案号数据就新增
                if (dto.getHrEmpBasic() != null) {
                    HrEmpBasic basic = dto.getHrEmpBasic();
                    if (basic.getUserCode() == null) {
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "工号不能为空!");
                    }

                    // 先查询是否存在该工号的记录
                    LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(HrEmpBasic::getUserCode, basic.getUserCode());
                    HrEmpBasic existingBasic = hrEmpBasicService.getOne(queryWrapper);

                    if (existingBasic == null) {
                        // 新增逻辑
                        try {
                            basic.setId(IdUtil.simpleUUID());
                            boolean saveResult = hrEmpBasicService.save(basic);
                            if (!saveResult) {
                                log.error("新增员工信息失败，工号：{}", basic.getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "新增员工信息失败!");
                            }
                            log.info("新增员工信息成功，工号：{}", basic.getUserCode());
                        } catch (Exception e) {
                            log.error("新增员工信息异常，工号：{}，异常信息：{}", basic.getUserCode(), e.getMessage());
                            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "新增员工信息异常!");
                        }
                    } else {
                        // 原有更新逻辑
                        // 如果传入status不为空，就更新status，反之不更新status
                        if(StringUtils.isNotBlank(basic.getStatus())) {
                            UpdateWrapper<HrEmpBasic> statusWrapper = new UpdateWrapper<>();
                            statusWrapper.eq("user_code", basic.getUserCode())
                                    .set("status", basic.getStatus());

                            boolean statusUpdateResult = hrEmpBasicService.update(statusWrapper);
                            if (!statusUpdateResult) {
                                log.error("更新员工状态失败，工号：{}", basic.getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工状态失败!");
                            }
                        }

                        // 如果没有提中干时间，且传入了提中干时间，那么更新提中干时间
                        if(existingBasic.getPromotionTime() == null && basic.getPromotionTime() != null) {
                            UpdateWrapper<HrEmpBasic> promotionTimeUpdate = new UpdateWrapper<>();
                            promotionTimeUpdate.eq("user_code", basic.getUserCode())
                                    .set("promotion_time", basic.getPromotionTime());

                            boolean promotionTimeUpdateResult = hrEmpBasicService.update(promotionTimeUpdate);
                            if (!promotionTimeUpdateResult) {
                                log.error("更新员工提中干时间失败，工号：{}", basic.getUserCode());
                                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工提中干时间失败!");
                            }
                        }

                        // 更新其他非空字段（排除id和userCode）
                        UpdateWrapper<HrEmpBasic> updateWrapper = UpdateWrapperUtils
                                .buildNonNullUpdateWrapper(basic, "id", "userCode", "status", "promotionTime") // 排除status和提中干时间字段，避免重复更新
                                .eq("user_code", basic.getUserCode());

                        boolean updateResult = hrEmpBasicService.update(updateWrapper);
                        if (!updateResult) {
                            log.error("更新基本信息失败，工号：{}", basic.getUserCode());
                            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新基本信息失败!");
                        }
                    }
                }

                //任职信息
                if (dto.getHrEmpAppointments() != null && !dto.getHrEmpAppointments().isEmpty()) {
                    // 更新之前任职信息的结束时间
                    // 选择出所有任职信息的结束时间为空的数据
                    List<HrEmpAppointment> undoneList = hrEmpAppointmentService.selectUndoneApp(dto.getHrEmpAppointments().get(0).getUserCode());
                    if (!ObjectUtils.isEmpty(undoneList)) {
                        for (HrEmpAppointment temp : undoneList) {
                            temp.setEndDate(dto.getHrEmpAppointments().get(0).getStartDate());
                        }
                        hrEmpAppointmentService.updateBatchById(undoneList);
                    }
                    // 保存新的任职信息
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
                if (dto.getHrEmpOtherAppointments() != null && !dto.getHrEmpOtherAppointments().isEmpty()){
                    //更新之前任职信息的结束时间
                    List<HrEmpOtherAppointment> undoneList = hrEmpOtherAppointmentService.selectUndoneApp(dto.getHrEmpOtherAppointments().get(0).getUserCode());
                    if (!ObjectUtils.isEmpty(undoneList)) {
                        for (HrEmpOtherAppointment temp : undoneList) {
                            temp.setEndDate(dto.getHrEmpOtherAppointments().get(0).getStartDate());
                        }
                        hrEmpOtherAppointmentService.updateBatchById(undoneList);
                    }

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
                    Map<String, String> booleanTranslateMap = new HashMap<>();
                    booleanTranslateMap.put("是", "1");
                    booleanTranslateMap.put("否", "0");
                    // 处理isHighest
                    HrEmpProfessionalQualification hrEmpProfessionalQualification = dto.getHrEmpProfessionalQualification();
                    String highest = booleanTranslateMap.getOrDefault(hrEmpProfessionalQualification.getIsHighest(), hrEmpProfessionalQualification.getIsHighest());
                    String appointed = booleanTranslateMap.getOrDefault(hrEmpProfessionalQualification.getIsAppointed(), hrEmpProfessionalQualification.getIsAppointed());
                    hrEmpProfessionalQualification.setIsHighest(highest);
                    hrEmpProfessionalQualification.setIsAppointed(appointed);
                    // 将其他的isHighest置为0
                    if("1".equals(hrEmpProfessionalQualification.getIsHighest())) {
                        List<HrEmpProfessionalQualification> hrEmpProfessionalQualifications = hrEmpProfessionalQualificationService.selectMsg(hrEmpProfessionalQualification.getUserCode());
                        List<HrEmpProfessionalQualification> list = hrEmpProfessionalQualifications.stream().filter(e -> "是".equals(e.getIsHighest()) || "1".equals(e.getIsHighest())).collect(Collectors.toList());
                        list.forEach(item -> {
                            item.setIsHighest("0");
                            hrEmpProfessionalQualificationService.updateById(item);
                        });
                    }
                    // 如果存在
                    boolean saveResult = hrEmpProfessionalQualificationService.save(dto.getHrEmpProfessionalQualification());
                    if (!saveResult) {
                        log.error("保存专业职务任职资格失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存专业职务任职资格失败!");
                    }
                }

                //员工履历信息表
                if (dto.getHrEmpResumes() != null && !dto.getHrEmpResumes().isEmpty()){
                    // 获取工号
                    HrEmpResume hrEmpResume = dto.getHrEmpResumes().get(0);
                    // 选择所有该工号，结束时间为null的数据
                    LambdaQueryWrapper<HrEmpResume> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.isNull(HrEmpResume::getEndDate);
                    queryWrapper.eq(HrEmpResume::getUserCode, hrEmpResume.getUserCode());
                    List<HrEmpResume> hrEmpResumeList = hrEmpResumeService.getBaseMapper().selectList(queryWrapper);
                    if(!ObjectUtils.isEmpty(hrEmpResumeList)) {
                        // 设置结束时间为下一履历的开始时间
                        hrEmpResumeList.forEach(
                            item -> item.setEndDate(hrEmpResume.getStartDate())
                        );
                    }

                    hrEmpResumeService.saveOrUpdateBatch(hrEmpResumeList);

                    for (HrEmpResume resume : dto.getHrEmpResumes()) {
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
                    String coefficient = dto.getHrEmpSalaryStandard().getCoefficient();
                    try {
                        BigDecimal coefficientBigDecimal = BigDecimal.valueOf(Long.parseLong(coefficient));
                        BigDecimal divide = coefficientBigDecimal.divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
                        dto.getHrEmpSalaryStandard().setCoefficient(divide.stripTrailingZeros().toPlainString());
                    } catch (Exception e) {
                        log.error("处理coefficient出现错误{}", e.getMessage(), e);
                    }
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

                // 员工薪资信息表 - 按工号更新，或者新增
                if (dto.getHrEmpSalaryInfo() != null) {
                    HrEmpSalaryInfo salaryInfo = dto.getHrEmpSalaryInfo();
                    if (salaryInfo.getUserCode() == null) {
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "工号不能为空!");
                    }

                    // 先查询数据库中是否已存在该工号的记录
                    LambdaQueryWrapper<HrEmpSalaryInfo> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(HrEmpSalaryInfo::getUserCode, salaryInfo.getUserCode());
                    HrEmpSalaryInfo existingRecord = hrEmpSalaryInfoService.getOne(queryWrapper);

                    if (existingRecord != null) {
                        // 如果存在记录，执行更新操作
                        UpdateWrapper<HrEmpSalaryInfo> updateWrapper = UpdateWrapperUtils
                                .buildNonNullUpdateWrapper(salaryInfo, "id", "userCode")
                                .eq("user_code", salaryInfo.getUserCode());

                        boolean updateResult = hrEmpSalaryInfoService.update(updateWrapper);
                        if (!updateResult) {
                            log.error("更新员工薪资信息失败，工号：{}", salaryInfo.getUserCode());
                            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工薪资信息失败!");
                        }
                    } else {
                        // 如果不存在记录，执行新增操作
                        salaryInfo.setId(IdUtil.simpleUUID());
                        boolean saveResult = hrEmpSalaryInfoService.save(salaryInfo);
                        if (!saveResult) {
                            log.error("新增员工薪资信息失败，工号：{}", salaryInfo.getUserCode());
                            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "新增员工薪资信息失败!");
                        }
                    }
                }
                updateFlag++;
            } catch (Exception e) {
                log.error("处理人事令信息异常，工号：{}，异常信息：{}",
                        dto.getHrEmpBasic() != null ? dto.getHrEmpBasic().getUserCode() : "未知",
                        e.getMessage(), e);
                throw new RuntimeException("处理人事令信息异常：" + e.getMessage());
            } finally {
                MultiTenantHandler.tenantId.remove();
            }
        }
        // 设置租户id
        MultiTenantHandler.tenantId.set("438");
        try {
            List<HrEmpBasic> userList = dtoList.stream().map(KingDiePersonalOrderDTO::getHrEmpBasic).collect(Collectors.toList());
            // 统计之前的人员分类1、4、7、8
            List<HrEmpBasic> categoryInfoList = hrEmpBasicService.getCategoryInfo(userList);
            Map<String, HrEmpBasic> hrEmpBasicMap = categoryInfoList.stream().collect(Collectors.toMap(
                    HrEmpBasic::getUserCode,
                    hrEmpBasic -> hrEmpBasic
            ));

            log.info("旧人员分类统计完毕，数量：{}", hrEmpBasicMap.size());
            hrEmpBasicController.syncPersonCategory(userList);
            // 统计之后的人员分类1、4、7、8
            List<HrEmpBasic> categoryInfoNewList = hrEmpBasicService.getCategoryInfo(userList);
            List<CategoryRecord> categoryRecordList = new ArrayList<>();
            categoryInfoNewList.forEach(newRecord -> {
                HrEmpBasic oldRecord = hrEmpBasicMap.get(newRecord.getUserCode());
                categoryRecordList.add(CategoryRecord.of(newRecord.getUserCode(), oldRecord, newRecord));
            });
            log.info("新人员分类统计完毕，数量：{}", categoryRecordList.size());

            categoryRecordService.saveBatch(categoryRecordList);

            log.info("日志保存完毕");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("处理人事令信息异常：" + e.getMessage());
        } finally {
            MultiTenantHandler.tenantId.remove();
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "人事令下达成功，信息已变更！ 变更数量： " + updateFlag);
    }

    /**
     * 使用用户名获得用户人脸识别的图片url
     * @param userCode 用户档案号
     */
    @IgnoreTenant
    @GetMapping("/getAttendanceImage")
    public ResponseResult<String> getAttendanceImage(@RequestParam String userCode) {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HrEmpBasic::getUserCode, userCode);
        HrEmpBasic hrEmpBasic = hrEmpBasicService.getBaseMapper().selectOne(queryWrapper);
        String fileUrl = minIOUtils.getFileUrl(hrEmpBasic.getAttendanceImageId());
        if(StringUtils.isNotBlank(fileUrl) && fileUrl.startsWith("http")) {
            log.info("getAttendanceImage OK:{}", new ResponseResult<>(HttpStatus.OK.value(), "OK", fileUrl));
            return new ResponseResult<>(HttpStatus.OK.value(), "OK", fileUrl);
        }
        log.info("getAttendanceImage ERROR:{}", new ResponseResult<>(HttpStatus.OK.value(), "OK", fileUrl));
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "ERROR");
    }

    @GetMapping("/file/download")
    @Operation(summary = "图片下载")
    @AuditLog(value = "图片下载", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @IgnoreTenant
    public void downloadPicture(@RequestParam String userCode, HttpServletResponse response) {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HrEmpBasic::getUserCode, userCode);
        HrEmpBasic hrEmpBasic = hrEmpBasicService.getBaseMapper().selectOne(queryWrapper);
        minIOUtils.getFile(hrEmpBasic.getAttendanceImageId(), response);
    }
}
