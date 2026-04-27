package com.crrc.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.client.PerRequireClient;
import com.crrc.entity.HrEmpSalaryInfo;
import com.crrc.entity.HrEmpSalaryStandard;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.listener.ImportExcelListener;
import com.crrc.mapper.HrEmpSalaryStandardMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.JobGrade;
import com.crrc.service.impl.HrEmpSalaryInfoServiceImpl;
import com.crrc.service.impl.HrEmpSalaryStandardServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 薪资标准 前端控制器
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@RestController
@RequestMapping("/hrEmpSalaryStandard")
@RefreshScope
@Tag(name = "薪资标准", description = "薪资标准")
@Slf4j
public class HrEmpSalaryStandardController {

    @Autowired
    private HrEmpSalaryStandardServiceImpl hrEmpSalaryStandardService;
    @Qualifier("com.crrc.client.PerRequireClient")
    @Autowired
    private PerRequireClient perRequireClient;
    @Autowired
    private HrEmpSalaryStandardMapper hrEmpSalaryStandardMapper;
    @Autowired
    private HrEmpSalaryInfoServiceImpl hrEmpSalaryInfoService;

    @GetMapping("/getDetails")
    @Operation(summary = "获得薪资标准实体")
    @AuditLog(value = "获得薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<List<HrEmpSalaryStandard>> getMsg(@RequestParam String userCode ) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表成功", hrEmpSalaryStandardService.selectMsg(userCode));
    }

    @PostMapping("/page")
    @Operation(summary = "获取薪资标准列表 page")
    @AuditLog(value = "获取薪资标准列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new HrEmpSalaryStandard() : pageReqVO;
        Page<HrEmpSalaryStandard> page = new Page<>(pageNo, pageSize);
        hrEmpSalaryStandardService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取薪资标准列表")
    @AuditLog(value = "获取薪资标准列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrEmpSalaryStandard>> queryHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new HrEmpSalaryStandard() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准列表成功", hrEmpSalaryStandardService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得薪资标准实体")
    @AuditLog(value = "获得薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrEmpSalaryStandard> getHrEmpSalaryStandard(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取薪资标准实体成功", hrEmpSalaryStandardService.getById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建薪资标准实体")
    @AuditLog(value = "创建薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrEmpSalaryStandard(@Valid @RequestBody HrEmpSalaryStandard createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建薪资标准实体成功", hrEmpSalaryStandardService.save(createReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新薪资标准实体")
    @AuditLog(value = "更新薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrEmpSalaryStandard(@Valid @RequestBody HrEmpSalaryStandard updateReqVO) {
        if(updateReqVO.getId() == null){
            updateReqVO.setId(IdUtil.simpleUUID());
            return new ResponseResult<>(HttpStatus.OK.value(), "创建员工基本信息表实体成功", hrEmpSalaryStandardService.save(updateReqVO));
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "更新薪资标准实体成功", hrEmpSalaryStandardService.saveOrUpdate(updateReqVO));
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除薪资标准实体")
    @AuditLog(value = "删除薪资标准实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrEmpSalaryStandard(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除薪资标准实体成功", hrEmpSalaryStandardService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出薪资标准报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出薪资标准报表")
    public void exportHrEmpSalaryStandard(@Validated @RequestBody(required = false) HrEmpSalaryStandard exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("薪资标准报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrEmpSalaryStandard() : exportReqVO;
        List<HrEmpSalaryStandard> hrEmpSalaryStandardList = hrEmpSalaryStandardService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrEmpSalaryStandard.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("薪资标准报表")
                .doWrite(hrEmpSalaryStandardList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入薪资标准报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入薪资标准报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrEmpSalaryStandard.class,
                new ImportExcelListener<HrEmpSalaryStandard, HrEmpSalaryStandardServiceImpl>(hrEmpSalaryStandardService))
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入薪资标准报表成功");
    }

    @PostMapping("/fixStandard")
    @Operation(summary = "修改与实际岗位工资不一致的薪资标准实体")
    public ResponseResult<Boolean> fixStandard(@Valid @RequestBody List<String> userCodeList) {

        // 1. 构建 positionSalary -> salaryGrade 的映射
        List<JobGrade> jobGradeList = perRequireClient.queryJobGrade(new JobGrade()).getData();
        if (CollUtil.isEmpty(jobGradeList)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "未获取到岗级数据", Boolean.FALSE);
        }

        Map<BigDecimal, String> salaryToGradeMap = jobGradeList.stream()
                .filter(jg -> jg.getPositionSalary() != null && StrUtil.isNotBlank(jg.getSalaryGrade()))
                .collect(Collectors.toMap(
                        JobGrade::getPositionSalary,
                        JobGrade::getSalaryGrade,
                        (existing, replacement) -> existing
                ));

        // 2. 如果没有传入userCodeList，则查询需要修正的用户列表
        if (CollUtil.isEmpty(userCodeList)) {
            userCodeList = hrEmpSalaryStandardMapper.findInconsistentUserCodes();
        }

        if (CollUtil.isEmpty(userCodeList)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "没有需要修正的数据", Boolean.TRUE);
        }

        // 3. 查询这些用户的薪资信息（获取positionSalary）
        List<HrEmpSalaryInfo> salaryInfoList = hrEmpSalaryInfoService.list(
                new LambdaQueryWrapper<HrEmpSalaryInfo>()
                        .in(HrEmpSalaryInfo::getUserCode, userCodeList)
                        .isNotNull(HrEmpSalaryInfo::getPositionSalary)
        );

        if (CollUtil.isEmpty(salaryInfoList)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "未找到对应的员工薪资信息", Boolean.TRUE);
        }

        // 4. 提取userCode列表，查询每个用户的最新薪资标准ID
        List<String> needFixUserCodes = salaryInfoList.stream()
                .map(HrEmpSalaryInfo::getUserCode)
                .collect(Collectors.toList());

        // 使用@Select查询最新的一条记录ID（与你的迁移SQL排序逻辑一致）
        List<String> latestStandardIds = hrEmpSalaryStandardMapper.findLatestStandardIds(needFixUserCodes);

        if (CollUtil.isEmpty(latestStandardIds)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "未找到需要更新的薪资标准记录", Boolean.TRUE);
        }

        // 查询这些最新记录
        List<HrEmpSalaryStandard> standardList = hrEmpSalaryStandardService.listByIds(latestStandardIds);
        Map<String, HrEmpSalaryStandard> standardMap = standardList.stream()
                .collect(Collectors.toMap(HrEmpSalaryStandard::getUserCode, v -> v));

        // 5. 遍历处理每个用户，只更新salaryGrade和coefficient
        List<HrEmpSalaryStandard> updateList = new ArrayList<>();
        int skipCount = 0;

        for (HrEmpSalaryInfo salaryInfo : salaryInfoList) {
            String userCode = salaryInfo.getUserCode();
            String positionSalaryStr = salaryInfo.getPositionSalary();

            if (StrUtil.isBlank(positionSalaryStr)) {
                skipCount++;
                continue;
            }

            BigDecimal positionSalary;
            try {
                positionSalary = new BigDecimal(positionSalaryStr.trim());
            } catch (NumberFormatException e) {
                log.info("岗位工资格式错误，userCode={}, positionSalary={}", userCode, positionSalaryStr);
                skipCount++;
                continue;
            }

            // 根据positionSalary找到对应的salaryGrade
            String salaryGrade = salaryToGradeMap.get(positionSalary);
            if (StrUtil.isBlank(salaryGrade)) {
                // 尝试抹零处理（去除小数点后多余的0并避免科学计数法）
                BigDecimal strippedPositionSalary = positionSalary.stripTrailingZeros();
                // 转换为普通数字格式
                strippedPositionSalary = new BigDecimal(strippedPositionSalary.toPlainString());
                salaryGrade = salaryToGradeMap.get(strippedPositionSalary);
                // 如果还是找不到，那就确实没有匹配记录了
                if (StrUtil.isBlank(salaryGrade)) {
                    log.info("未找到匹配的岗级记录，userCode={}, positionSalary={}", userCode, positionSalary);
                    skipCount++;
                    continue;
                }
            }

            // 获取该用户最新的薪资标准
            HrEmpSalaryStandard standard = standardMap.get(userCode);
            if (standard == null) {
                log.info("未找到薪资标准记录，userCode={}", userCode);
                skipCount++;
                continue;
            }

            // 计算coefficient（positionSalary / 100，抹零）
            BigDecimal newCoefficient = positionSalary.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            String coefficientStr = newCoefficient.stripTrailingZeros().toPlainString();

            // 只更新两个字段：salaryGrade（薪档）和 coefficient（执行系数/金额）
            standard.setSalaryGrade(salaryGrade);                    // 薪档 = 岗级
            standard.setCoefficient(coefficientStr);   // 执行系数/金额 = 岗位工资/100

            updateList.add(standard);
        }

        // 6. 批量更新
        if (CollUtil.isEmpty(updateList)) {
            return new ResponseResult<>(HttpStatus.OK.value(), "没有需要更新的记录", Boolean.TRUE);
        }

        boolean result = hrEmpSalaryStandardService.updateBatchById(updateList);

        String msg = String.format("修正完成：成功更新%d条，跳过%d条", updateList.size(), skipCount);
        return new ResponseResult<>(HttpStatus.OK.value(), msg, result);
    }

}
