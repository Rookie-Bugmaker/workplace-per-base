package com.crrc.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.entity.*;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.mapper.HrEmpSalaryInfoMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.KingDiePersonalOrderDTO;
import com.crrc.service.CategoryRecordService;
import com.crrc.service.impl.*;
import com.crrc.utils.MinIOUtils;
import com.crrc.utils.SystemConstants;
import com.crrc.utils.UpdateWrapperUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.crrc.constants.HrOrderType;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private MinIOUtils minioUtils;
    @Autowired
    private HrEmpSalaryInfoMapper hrEmpSalaryInfoMapper;

    @PostMapping("/receiveKingDieMsg")
    @Operation(summary = "接收金蝶传递人事令信息")
    @AuditLog(value = "接收金蝶传递人事令信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Integer> receiveKingDieMsg(@RequestBody List<KingDiePersonalOrderDTO> dtoList) {

        int updateFlag = 0;
        for (KingDiePersonalOrderDTO dto : dtoList) {
            try {
                MultiTenantHandler.tenantId.set("438");
                // 员工基本信息表 - 按工号更新或新增，先查询，如果没有该档案号数据就新增
                // 处理人员分类8，如果dto.getHrOrderType()为空说明不需要动人员分类8
                if(StringUtils.isNotBlank(dto.getHrOrderType())) {
                    switch(dto.getHrOrderType()) {
                        // 长病
                        case HrOrderType.LONG_ILLNESS:
                            // 劳
                            dto.getHrEmpBasic().setCategory8("003");
                            break;
                        // 长伤
                        case HrOrderType.LONG_INJURY:
                            // 伤
                            dto.getHrEmpBasic().setCategory8("005");
                            break;
                        // 借调
                        case HrOrderType.LOAN_ASSIGNMENT:
                            // 借
                            dto.getHrEmpBasic().setCategory8("002");
                            break;
                        // 外派
                        case HrOrderType.EXPATRIATE_ASSIGNMENT:
                            // 派
                            dto.getHrEmpBasic().setCategory8("004");
                            break;
                        // 挂职/助勤
                        case HrOrderType.SUPPORT_ASSIGNMENT:
                            // 助
                            dto.getHrEmpBasic().setCategory8("008");
                            break;
                        // 下岗
                        case HrOrderType.OFF_POST:
                            // 待
                            dto.getHrEmpBasic().setCategory8("001");
                            break;
                        // 退养
                        case HrOrderType.RETIREMENT:
                            // 养
                            dto.getHrEmpBasic().setCategory8("007");
                            break;
                        case HrOrderType.LONG_ILLNESS_END:
                        case HrOrderType.LONG_INJURY_END:
                        case HrOrderType.LOAN_ASSIGNMENT_END:
                        case HrOrderType.EXPATRIATE_ASSIGNMENT_END:
                        case HrOrderType.SUPPORT_ASSIGNMENT_END:
                        case HrOrderType.UP_POST:
                            // 现
                            dto.getHrEmpBasic().setCategory8("006");
                            break;
                    }
                }

                // 这里会更新人员身份（人员分类4）
                if (dto.getHrEmpBasic() != null) {
                    HrEmpBasic basic = dto.getHrEmpBasic();
                    if (basic.getUserCode() == null) {
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "工号不能为空!");
                    }
                    // 如果死亡时间不为空，将人员放入离职库中
                    if(!ObjectUtils.isEmpty(basic.getDeathTime())) {
                        basic.setIsEmployed(SystemConstants.FALSE_FLAG);
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
                    // 选择出所有任职信息的结束时间为空或者任职结束时间在2198年之后的数据
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
                if (dto.getHrEmpCareerPath() != null && dto.getHrEmpCareerPath().getUserCode() != null && StringUtils.isNotBlank(dto.getHrEmpCareerPath().getPathId()) && StringUtils.isNotBlank(dto.getHrEmpCareerPath().getLevelId())) {
                    // 将该用户其他职业发展通道设置为过期
                    String userCode = dto.getHrEmpCareerPath().getUserCode();
                    LambdaQueryWrapper<HrEmpCareerPath> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(HrEmpCareerPath::getUserCode, userCode);
                    List<HrEmpCareerPath> hrEmpCareerPaths = hrEmpCareerPathService.getBaseMapper().selectList(wrapper);
                    for(HrEmpCareerPath path : hrEmpCareerPaths) {
                        if("0".equals(path.getIsExpired())) {
                            path.setIsExpired("1");
                            hrEmpCareerPathService.updateById(path);
                        }
                    }
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
                if (dto.getHrEmpProfessionalQualification() != null && dto.getHrEmpProfessionalQualification().getUserCode() != null && StringUtils.isNotBlank(dto.getHrEmpProfessionalQualification().getQuaApprovalUnit()) && StringUtils.isNotBlank(dto.getHrEmpProfessionalQualification().getQuaPath())) {
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
                    // 如果这里的highest是'1', 将其他的isHighest置为0
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
                    // 选择所有该工号，结束时间为null，或者结束时间在2198年之后的的数据
                    LambdaQueryWrapper<HrEmpResume> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.and(
                            w -> w.isNull(HrEmpResume::getEndDate)
                            .or()
                            .gt(HrEmpResume::getEndDate, "2198-01-01")
                    );
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
                // 岗级不可能为空
                if (dto.getHrEmpSalaryStandard() != null && dto.getHrEmpSalaryStandard().getUserCode() != null && dto.getHrEmpSalaryStandard().getSalaryGrade() != null && dto.getHrEmpSalaryStandard().getCoefficient() != null) {
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
                    HrEmpSkillLevel hrEmpSkillLevel = dto.getHrEmpSkillLevel();

                    // 如果这里的highest是'1', 将其他的isHighest置为0
                    if("1".equals(hrEmpSkillLevel.getIsHighest())) {
                        List<HrEmpSkillLevel> hrEmpSkillLevels = hrEmpSkillLevelService.selectMsg(hrEmpSkillLevel.getUserCode());
                        List<HrEmpSkillLevel> list = hrEmpSkillLevels.stream().filter(e -> "是".equals(e.getIsHighest()) || "1".equals(e.getIsHighest())).collect(Collectors.toList());
                        list.forEach(item -> {
                            item.setIsHighest("0");
                            hrEmpSkillLevelService.updateById(item);
                        });
                    }
                    // 是否聘任也只保留一个
                    if("1".equals(hrEmpSkillLevel.getIsAppointed())) {
                        List<HrEmpSkillLevel> hrEmpSkillLevels = hrEmpSkillLevelService.selectMsg(hrEmpSkillLevel.getUserCode());
                        List<HrEmpSkillLevel> list = hrEmpSkillLevels.stream().filter(e -> "是".equals(e.getIsAppointed()) || "1".equals(e.getIsAppointed())).collect(Collectors.toList());
                        list.forEach(item -> {
                            item.setIsAppointed("0");
                            hrEmpSkillLevelService.updateById(item);
                        });
                    }

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
                        BeanUtil.copyProperties(dto.getHrEmpSalaryInfo(), existingRecord, CopyOptions.create().setIgnoreNullValue(true));
                        // 如果存在记录，执行更新操作
//                        UpdateWrapper<HrEmpSalaryInfo> updateWrapper = UpdateWrapperUtils
//                                .buildNonNullUpdateWrapper(salaryInfo, "id", "userCode")
//                                .eq("user_code", salaryInfo.getUserCode());

                        boolean updateResult = hrEmpSalaryInfoService.updateById(existingRecord);
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
            if(!ObjectUtils.isEmpty(dtoList.stream().filter(e -> e.getHrEmpBasic() != null).collect(Collectors.toList()))) {
                List<HrEmpBasic> userList = dtoList.stream().map(KingDiePersonalOrderDTO::getHrEmpBasic).collect(Collectors.toList());
                // 统计之前的人员分类1、4、7、8
//                List<HrEmpBasic> categoryInfoList = hrEmpBasicService.getCategoryInfo(userList);
//                Map<String, HrEmpBasic> hrEmpBasicMap = categoryInfoList.stream().collect(Collectors.toMap(
//                        HrEmpBasic::getUserCode,
//                        hrEmpBasic -> hrEmpBasic
//                ));
//
//                log.info("旧人员分类统计完毕，数量：{}", hrEmpBasicMap.size());
                // 处理人员分类1和7
                hrEmpBasicController.syncPersonCategory(userList);
                // 统计之后的人员分类1、4、7、8
//                List<HrEmpBasic> categoryInfoNewList = hrEmpBasicService.getCategoryInfo(userList);
//                List<CategoryRecord> categoryRecordList = new ArrayList<>();
//                categoryInfoNewList.forEach(newRecord -> {
//                    HrEmpBasic oldRecord = hrEmpBasicMap.get(newRecord.getUserCode());
//                    categoryRecordList.add(CategoryRecord.of(newRecord.getUserCode(), oldRecord, newRecord));
//                });
//                log.info("新人员分类统计完毕，数量：{}", categoryRecordList.size());
//
//                categoryRecordService.saveBatch(categoryRecordList);

                //log.info("日志保存完毕");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
    public ResponseEntity<String> getAttendanceImage(@RequestParam String userCode) {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HrEmpBasic::getUserCode, userCode);
        HrEmpBasic hrEmpBasic = hrEmpBasicService.getBaseMapper().selectOne(queryWrapper);
        String fileUrl = minioUtils.getFileUrl(hrEmpBasic.getAttendanceImageId());
        if(StringUtils.isNotBlank(fileUrl) && fileUrl.startsWith("http")) {
            log.info("getAttendanceImage OK:{}", new ResponseResult<>(HttpStatus.OK.value(), "OK", fileUrl));
            return new ResponseEntity<>(fileUrl, HttpStatus.OK);
        }
        log.info("getAttendanceImage ERROR:{}", new ResponseResult<>(HttpStatus.OK.value(), "OK", fileUrl));
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/file/download")
    @Operation(summary = "图片下载")
    @AuditLog(value = "图片下载", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @IgnoreTenant
    public void downloadPicture(@RequestParam String userCode, HttpServletResponse response) {
        LambdaQueryWrapper<HrEmpBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HrEmpBasic::getUserCode, userCode);
        HrEmpBasic hrEmpBasic = hrEmpBasicService.getBaseMapper().selectOne(queryWrapper);
        minioUtils.getFile(hrEmpBasic.getAttendanceImageId(), response);
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

    @PostMapping("/updateSalaryInfoName")
    @Operation(summary = "更新员工薪酬信息姓名")
    @AuditLog(value = "更新员工薪酬信息姓名", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @IgnoreTenant
    public ResponseResult<String> updateSalaryInfoName() {
        try {
            hrEmpSalaryInfoMapper.syncNameFromBasic();
            return new ResponseResult<>(HttpStatus.OK.value(), "更新员工薪酬信息姓名成功");
        } catch (Exception e) {
            log.error("更新员工薪酬信息姓名失败", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新员工薪酬信息姓名失败");
        }
    }

    @PostMapping("/updateCategory8")
    @Operation(summary = "单独重算指定人员的人员分类8")
    @AuditLog(value = "更新员工薪酬信息姓名", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @IgnoreTenant
    public ResponseResult<String> updateCategory8(@RequestBody List<String> userCodeList, @RequestParam(required = false) String category8) {
        try {
            StringBuilder result = new StringBuilder();

            for(String userCode : userCodeList) {
                HrEmpBasic entity = hrEmpBasicService.getByUserCode(userCode);
                if(StringUtils.isNotBlank(category8)) {
                    entity.setCategory8(category8);
                    result.append("档案号：").append(userCode).append(" 人员分类8：").append(category8);
                } else {
//                    String category8Mark = hrEmpBasicController.resolveCategory8(entity);
//                    entity.setCategory8(category8Mark);
//                    result.append("档案号：").append(userCode).append(" 人员分类8：").append(category8Mark);
                    throw new RuntimeException("未传入人员分类8");
                }
                hrEmpBasicService.updateById(entity);
            }
            return new ResponseResult<>(HttpStatus.OK.value(), "重算指定人员的人员分类8成功" + result);
        } catch (Exception e) {
            log.error("重算指定人员的人员分类8失败", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "重算指定人员的人员分类8失败");
        }
    }
}
