package com.crrc.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.annotation.TenantIgnore;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.*;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.mapper.HrEmpSalaryInfoMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpForeignLanguageDTO;
import com.crrc.pojo.dto.KingDiePersonalOrderDTO;
import com.crrc.service.CategoryRecordService;
import com.crrc.service.impl.*;
import com.crrc.utils.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private MinIOUtils minioUtils;
    @Autowired
    private HrEmpSalaryInfoMapper hrEmpSalaryInfoMapper;
    @Autowired
    private ExceptionCasePersonServiceImpl exceptionCasePersonService;
    @Autowired
    private WebServiceUtil webServiceUtil;
    @Autowired
    private TemplateGenerateServiceImpl templateGenerateServiceImpl;
    @Autowired
    private HrEmpCadreAppointmentInfoServiceImpl hrEmpCadreAppointmentInfoService;
    @Autowired
    private HrEmpBankServiceImpl hrEmpBankService;
    @Autowired
    private HrEmpCertificateServiceImpl hrEmpCertificateService;
    @Autowired
    private HrEmpCommunicationServiceImpl hrEmpCommunicationService;
    @Autowired
    private HrEmpEducationServiceImpl hrEmpEducationService;
    @Autowired
    private HrEmpFamMemberServiceImpl hrEmpFamMemberService;
    @Autowired
    private HrEmpHealthServiceImpl hrEmpHealthService;
    @Autowired
    private HrEmpHouseholdServiceImpl hrEmpHouseholdService;
    @Autowired
    private HrEmpForeignLanguageServiceImpl hrEmpForeignLanguageService;
    @Autowired
    private MessageUtils messageUtils;

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

                    // 排序，order by startDate desc nulls last
                    undoneList.sort(Comparator.comparing(
                            HrEmpAppointment::getStartDate,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ));

                    // 获得当前生效的职种
                    String jobType = undoneList.get(0).getJobType();

                    // 判断人员的职类职群职种是否为空
                    if(dto.getHrEmpAppointments().stream().anyMatch(e -> StringUtils.isBlank(e.getJobType()))) {
                        String message;
                        if(StringUtils.isBlank(jobType)) {
                            message = String.format(
                                    "员工: %s\n职类职群职种为空，且当前生效职种为空！请立即核验",
                                    Optional.ofNullable(dto.getHrEmpBasic()).orElse(HrEmpBasic.builder().userCode("HrEmpBasic为空").build()).getUserCode()
                            );
                        } else {
                            message = String.format(
                                    "员工: %s\n职类职群职种为空，自动带入当前生效的职类职群职种:%s\n",
                                    Optional.ofNullable(dto.getHrEmpBasic()).orElse(HrEmpBasic.builder().userCode("HrEmpBasic为空").build()).getUserCode(),
                                    jobType
                            );
                        }

                        dto.getHrEmpAppointments()
                                .stream()
                                .filter(e -> StringUtils.isBlank(e.getJobType()))
                                .forEach(item -> item.setJobType(jobType));

                        // 发融数消息
                        messageUtils.pushPlainMsg(
                                Arrays.asList("013200031524", "013200031572", "013200031192"),
                                message
                        );
                    }

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
                            path.setExpiryDate(dto.getHrEmpCareerPath().getEffectiveDate());
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
                    // 如果这里的appointed是'1', 将其他的isAppointed置为0，同时将聘任结束时间回填
                    if("1".equals(hrEmpProfessionalQualification.getIsAppointed())) {
                        List<HrEmpProfessionalQualification> hrEmpProfessionalQualifications = hrEmpProfessionalQualificationService.selectMsg(hrEmpProfessionalQualification.getUserCode());
                        hrEmpProfessionalQualifications.forEach(item -> {
                            // 其他的isAppointed置为0
                            if("是".equals(item.getIsAppointed()) || "1".equals(item.getIsAppointed())) {
                                item.setIsAppointed("0");
                            }
                            // 回填聘任结束时间
                            if(item.getAppointedEndDate() == null || item.getAppointedEndDate().isAfter(LocalDate.parse("2098-12-31", DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
                                item.setAppointedEndDate(hrEmpProfessionalQualification.getAppointedStartDate());
                            }
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

                    // 把开始时间最近的一条，赋予结束时间
                    List<HrEmpSkillLevel> list = hrEmpSkillLevelService.selectMsg(hrEmpSkillLevel.getUserCode());
                    HrEmpSkillLevel recentEntity = list.get(list.size() - 1);
                    recentEntity.setAppointedEndDate(hrEmpSkillLevel.getAppointedStartDate());
                    hrEmpSkillLevelService.updateById(recentEntity);

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

                if(!ObjectUtils.isEmpty(dto.getHrEmpCadreAppointmentInfos())) {
                    List<String> userCodes = dto.getHrEmpCadreAppointmentInfos().stream().map(HrEmpCadreAppointmentInfo::getUserCode).collect(Collectors.toList());
                    if(!ObjectUtils.isEmpty(userCodes)) {
                        hrEmpCadreAppointmentInfoService
                                .lambdaUpdate()
                                .in(HrEmpCadreAppointmentInfo::getUserCode, userCodes)
                                .remove();
                    }
                    dto.getHrEmpCadreAppointmentInfos().forEach(item -> {
                        if(StringUtils.isBlank(item.getId())) {
                            item.setId(IdUtil.randomUUID());
                        }
                    });
                    hrEmpCadreAppointmentInfoService.saveOrUpdateBatch(dto.getHrEmpCadreAppointmentInfos());
                }

                // 保存增员令的信息
                // 银行卡
                if(!ObjectUtils.isEmpty(dto.getHrEmpBank())) {
                    if(StringUtils.isBlank(dto.getHrEmpBank().getId())) {
                        dto.getHrEmpBank().setId(IdUtil.randomUUID());
                    }
                    // 银行账户信息全删全插
                    hrEmpBankService.lambdaUpdate()
                            .eq(HrEmpBank::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpBankService.saveOrUpdate(dto.getHrEmpBank());
                }

                // 身份证
                if(!ObjectUtils.isEmpty(dto.getHrEmpCertificate())) {
                    // 只有一条，全删全插
                    if(StringUtils.isBlank(dto.getHrEmpCertificate().getId())) {
                        dto.getHrEmpCertificate().setId(IdUtil.randomUUID());
                    }
                    // 身份证信息只允许一条
                    hrEmpCertificateService.lambdaUpdate()
                            .eq(HrEmpCertificate::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpCertificateService.saveOrUpdate(dto.getHrEmpCertificate());
                }

                // 联系方式
                if(!ObjectUtils.isEmpty(dto.getHrEmpCommunication())) {
                    // 只有一条，全删全插
                    if(StringUtils.isBlank(dto.getHrEmpCommunication().getId())) {
                        dto.getHrEmpCommunication().setId(IdUtil.randomUUID());
                    }
                    // 联系方式只允许一条
                    hrEmpCommunicationService.lambdaUpdate()
                            .eq(HrEmpCommunication::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpCommunicationService.saveOrUpdate(dto.getHrEmpCommunication());
                }

                // 教育经历
                if(!ObjectUtils.isEmpty(dto.getHrEmpEducations())) {
                    // 全删全插
                    dto.getHrEmpEducations().forEach(item -> {
                        if(StringUtils.isBlank(item.getId())) {
                            item.setId(IdUtil.randomUUID());
                        }
                    });

                    hrEmpEducationService.lambdaUpdate()
                            .eq(HrEmpEducation::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpEducationService.saveOrUpdateBatch(dto.getHrEmpEducations());
                }

                // 家庭成员信息
                if(!ObjectUtils.isEmpty(dto.getHrEmpFamMembers())) {
                    // 全删全插
                    dto.getHrEmpFamMembers().forEach(item -> {
                        if(StringUtils.isBlank(item.getId())) {
                            item.setId(IdUtil.randomUUID());
                        }
                    });

                    hrEmpFamMemberService.lambdaUpdate()
                            .eq(HrEmpFamMember::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpFamMemberService.saveOrUpdateBatch(dto.getHrEmpFamMembers());
                }

                // 员工健康
                if(!ObjectUtils.isEmpty(dto.getHrEmpHealth())) {
                    // 全删全插
                    if(StringUtils.isBlank(dto.getHrEmpHealth().getId())) {
                        dto.getHrEmpHealth().setId(IdUtil.randomUUID());
                    }


                    // 联系方式只允许一条
                    hrEmpHealthService.lambdaUpdate()
                        .eq(HrEmpHealth::getUserCode, dto.getHrEmpBasic().getUserCode())
                        .remove();

                    hrEmpHealthService.saveOrUpdate(dto.getHrEmpHealth());
                }

                // 户口本信息
                if(!ObjectUtils.isEmpty(dto.getHrEmpHouseHold())) {
                    // 全删全插

                    if(StringUtils.isBlank(dto.getHrEmpHouseHold().getId())) {
                        dto.getHrEmpHouseHold().setId(IdUtil.randomUUID());
                    }


                    // 联系方式只允许一条
                    hrEmpHouseholdService.lambdaUpdate()
                            .eq(HrEmpHouseHold::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpHouseholdService.saveOrUpdate(dto.getHrEmpHouseHold());
                }

                // 外语等级信息
                if(!ObjectUtils.isEmpty(dto.getHrEmpForeignLanguages())) {
                    // 全删全插
                    dto.getHrEmpForeignLanguages().forEach(item -> {
                        if(StringUtils.isBlank(item.getId())) {
                            item.setId(IdUtil.randomUUID());
                        }
                    });


                    // 联系方式只允许一条
                    hrEmpForeignLanguageService.lambdaUpdate()
                            .eq(HrEmpForeignLanguage::getUserCode, dto.getHrEmpBasic().getUserCode())
                            .remove();

                    hrEmpForeignLanguageService.saveOrUpdateBatch(dto.getHrEmpForeignLanguages());
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
                // 处理人员分类7
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

    @PostMapping("/query/exception/person")
    @IgnoreTenant
    public ResponseResult<List<HrEmpBasicDTO>> queryAllExceptionPerson() {
        MPJLambdaWrapper<ExceptionCasePerson> wrapper = new MPJLambdaWrapper<>();
        Date now = new Date();

        wrapper.selectAll(ExceptionCasePerson.class);
        wrapper.leftJoin(
                ExceptionCasePersonRelation.class,
                on -> on.eq(ExceptionCasePerson::getRelationId, ExceptionCasePersonRelation::getId)
        ).selectAll(ExceptionCasePersonRelation.class);

        wrapper.le(ExceptionCasePerson::getStartDate, now);
        wrapper.ge(ExceptionCasePerson::getEndDate, now);
        wrapper.eq(ExceptionCasePersonRelation::getStatus, AuditConstants.STATUS_AUDIT_PASS);

        List<ExceptionCasePerson> exceptionCasePeople = exceptionCasePersonService.getBaseMapper().selectList(wrapper);

        List<String> userCodeList = exceptionCasePeople.stream().map(ExceptionCasePerson::getUserCode).collect(Collectors.toList());

        Map<String, HrEmpBasicDTO> hrEmpBasicDTOS = hrEmpBasicService.selectMsgList(userCodeList).stream().collect(Collectors.toMap(
                HrEmpBasicDTO::getUserCode,
                e -> e,
                (a, b) -> a
        ));
        hrEmpBasicService.resolveFieldsList(new ArrayList<>(hrEmpBasicDTOS.values()));
        exceptionCasePeople.forEach(item -> hrEmpBasicDTOS.computeIfPresent(item.getUserCode(), (k, v) -> {BeanUtil.copyProperties(item, v, CopyOptions.create().setIgnoreNullValue(true)); return v;}));

        return new ResponseResult<>(HttpStatus.OK.value(), new ArrayList<>(hrEmpBasicDTOS.values()));
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
    @IgnoreTenant
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

    @PostMapping("/updateattendanceImage")
    @Operation(summary = "更新用户人脸识别（考勤打卡）的图片")
    @AuditLog(value = "更新用户人脸识别（考勤打卡）的图片", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @IgnoreTenant
    public ResponseResult<String> updateattendanceImage(@RequestParam String userCode) {
        try {
            hrEmpSalaryInfoMapper.updateattendanceImage(userCode);
            return new ResponseResult<>(HttpStatus.OK.value(), "更新用户人脸识别（考勤打卡）的图片成功");
        } catch (Exception e) {
            log.error("更新用户人脸识别（考勤打卡）的图片失败", e);
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "更新用户人脸识别（考勤打卡）的图片失败");
        }
    }

    @PostMapping("/queryForSap")
    @Operation(summary = "SAP webservice, 获取额外人员信息表列表")
    @AuditLog(value = "获取额外人员信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult queryForSap() {
        List<ExceptionCasePerson> effectiveList = exceptionCasePersonService.selectEffectivePerson();

        // 组装XML报文
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<Objects>");

        for (ExceptionCasePerson person : effectiveList) {
            xmlBuilder.append("    <item>\n")
                    .append("        <USERID>").append(person.getUserCode()).append("</USERID>")
                    .append("        <NAME>").append(person.getName()).append("</NAME>")
                    .append("        <COMPANY>").append(person.getNewCompanyCode()).append("</COMPANY>")
                    .append("        <DEPARTMENT>").append(person.getNewDeptCode()).append("</DEPARTMENT>")
                    .append("        <DEPARTNAME>").append(person.getNewDept()).append("</DEPARTNAME>")
                    .append("        <GENDER>").append("男").append("</GENDER>\n") // todo 目前没有收集性别信息，默认全传男
                    .append("        <CLASS>").append(resolveUserType(person.getSourceType())).append("</CLASS>")
                    .append("    </item>");
        }

        xmlBuilder.append("</Objects>");

        // 发送WebService请求
        String messageCode = "013200HRC000005";
        String xmlMsg = webServiceUtil.sendByWebService(xmlBuilder.toString(), messageCode);

        // 处理返回结果
        String result = webServiceUtil.xmlTransformer(xmlMsg);
        JSONObject jsonObject = JSONUtil.parseObj(result);

        if("S".equals(jsonObject.get("M_TYPE"))) {
            return new ResponseResult<>(HttpStatus.OK.value(), DateTime.now() + "SAP接收成功", jsonObject);
        } else {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), DateTime.now() + "SAP接收失败", jsonObject);
        }
    }

    @PostMapping("/queryForKingdie")
    @Operation(summary = "接收总线WebService请求，返回人员信息XML")
    @AuditLog(value = "接收总线请求返回人员信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @TenantIgnore
    public ResponseEntity<String> queryForKingdie() {
        // 1. 获取有效人员列表
        List<ExceptionCasePerson> effectiveList = exceptionCasePersonService.selectEffectivePerson();

        // 2. 组装XML报文
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<USERS>");

        for (ExceptionCasePerson person : effectiveList) {
            xmlBuilder.append("    <USER>")
                    .append("        <USER_CODE>").append(person.getUserCode()).append("</USER_CODE>")
                    .append("        <NAME>").append(person.getName()).append("</NAME>")
                    .append("        <ORG>").append(person.getNewCompanyCode()).append("</ORG>")
                    .append("        <DEPT>").append(person.getNewDeptCode()).append("</DEPT>")
                    .append("        <DUTYPE>").append(resolveUserType(person.getSourceType())).append("</DUTYPE>")
                    .append("    </USER>");
        }

        xmlBuilder.append("</USERS>");


        // 3. 设置响应头为XML格式
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        // 4. 返回XML响应
        return new ResponseEntity<>(xmlBuilder.toString(), headers, HttpStatus.OK);
    }

    /**
     * 处理职能标识
     * @param userType 职能标识编码
     * @return 转换后的职能标识汉字
     */
    private String resolveUserType(String userType) {
        if(userType == null) {
            return "外派股东";
        }
        // todo 如果后面换了逻辑，记得处理这里
        switch (userType) {
            case "EXT_SEC":
                userType = "外派股东";
                break;
            case "INT_LOAN":
                userType = "借用";
                break;
            case "SIDE_POSITION":
                userType = "兼职";
                break;
            default:
                userType = "未知类型";
        }
        return userType;
    }

    @PostMapping("/testGene")
    public ResponseResult<String> testGene(@RequestParam String id) {
        templateGenerateServiceImpl.generateRequestWordAndUpdate(id, 0, 4);
        return ResponseResult.success("OK");
    }
}
