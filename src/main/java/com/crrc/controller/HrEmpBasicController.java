package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.HrEmpHealth;
import com.crrc.entity.HrEmpHouseHold;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.pojo.dto.KingDiePersonalOrderDTO;
import com.crrc.service.impl.*;
import com.crrc.utils.UpdateWrapperUtils;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import java.util.List;

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
    @Operation(summary = "接收金蝶回传人事令信息")
    @AuditLog(value = "接收金蝶回传人事令信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Integer> receiveKingDieMsg(@RequestBody List<KingDiePersonalOrderDTO> dtoList) {
        int updateFlag = 0;
        for (KingDiePersonalOrderDTO dto : dtoList) {
            try {
                //任职信息
                if (dto.getHrEmpAppointment() != null && dto.getHrEmpAppointment().getUserCode() != null) {
                    dto.getHrEmpAppointment().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpAppointmentService.save(dto.getHrEmpAppointment());
                    if (!saveResult) {
                        log.error("保存任职信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存任职信息失败!");
                    }
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
                if (dto.getHrEmpCareerPath() != null && dto.getHrEmpCareerPath().getUserCode() != null)
                    dto.getHrEmpCareerPath().setId(IdUtil.simpleUUID());{
                    boolean saveResult = hrEmpCareerPathService.save(dto.getHrEmpCareerPath());
                    if (!saveResult) {
                        log.error("保存职业发展通道失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存职业发展通道失败!");
                    }
                }

                //其他任职信息
                if (dto.getHrEmpOtherAppointment() != null && dto.getHrEmpOtherAppointment().getUserCode() != null) {
                    dto.getHrEmpOtherAppointment().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpOtherAppointmentService.save(dto.getHrEmpOtherAppointment());
                    if (!saveResult) {
                        log.error("保存其他任职信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存其他任职信息失败!");
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
                    boolean saveResult = hrEmpProfessionalQualificationService.save(dto.getHrEmpProfessionalQualification());
                    if (!saveResult) {
                        log.error("保存专业职务任职资格失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存专业职务任职资格失败!");
                    }
                }

                //员工履历信息表
                if (dto.getHrEmpResume() != null && dto.getHrEmpResume().getUserCode() != null) {
                    dto.getHrEmpResume().setId(IdUtil.simpleUUID());
                    boolean saveResult = hrEmpResumeService.save(dto.getHrEmpResume());
                    if (!saveResult) {
                        log.error("保存履历信息失败，工号：{}", dto.getHrEmpBasic().getUserCode());
                        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存履历信息失败!");
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

    @GetMapping("/getDetails")
    @Operation(summary = "获得员工基本信息表实体")
    @AuditLog(value = "获得员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpBasicDTO>> getMsg(@RequestParam String userCode) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取员工基本信息表实体成功", hrEmpBasicService.selectMsg(userCode));
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

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新员工基本信息表实体")
    @AuditLog(value = "更新员工基本信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpBasic(@Valid @RequestBody HrEmpBasicDTO updateReqVO) {
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
            if (updateReqVO.getId() == null) {
                updateReqVO.setId(IdUtil.simpleUUID());
                hrEmpBasicService.save(updateReqVO);
            }
            hrEmpBasicService.saveOrUpdate(updateReqVO);
        } catch (Exception e) {
            log.error("更新或保存时出错：\n" + "入参： " + updateReqVO + "\n" + "错误日志： " + e.getMessage() + "\n");
            return new ResponseResult<>(HttpStatus.EXPECTATION_FAILED.value(), "更新员工教育信息表实体失败");
        }
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

}
