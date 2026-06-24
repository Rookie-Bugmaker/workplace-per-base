package com.crrc.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.HrEmpAppointment;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.pojo.vo.ImportResult;
import com.crrc.service.HrEmpAppointmentService;
import com.crrc.utils.DeptUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任职信息 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpAppointmentServiceImpl extends WorkServiceImpl<HrEmpAppointmentMapper, HrEmpAppointment> implements HrEmpAppointmentService {

    @Autowired
    private HrEmpAppointmentMapper hrEmpAppointmentMapper;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    public List<HrEmpAppointment> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .selectAll(HrEmpAppointment.class)
                .eq(HrEmpAppointment::getUserCode, userCode)
                .last("ORDER BY create_time::DATE ASC NULLS FIRST, sort_no ASC NULLS FIRST, " +
                        "start_date ASC NULLS FIRST");
        return hrEmpAppointmentMapper.selectList(wrapper);
    }

    /**
     * 数据库中用于识别“离职任职记录”的变动类型
     */
    private static final List<String> LEAVE_CHANGE_TYPES = Arrays.asList(
            "离职",
            "退休",
            "死亡"
    );

    /**
     * 导入离职员工生效记录，刷写对应任职记录的 changeType
     * 规则：
     * 1. 读取 Excel 每一行；
     * 2. 根据 userCode 找到该员工数据库中 changeType 属于 离职/退休/死亡 的任职记录；
     * 3. 将该记录的 changeType 修改为 Excel 行中的 changeType；
     * 4. 任一行失败则整批回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importLeaveAppointmentChangeType(MultipartFile file) {
        Map<String, String> codeInfoMap = codeInfoService.getCodeInfoList("hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE")
                .stream()
                .collect(Collectors.toMap(
                        CodeInfo::getCodeValue,
                        CodeInfo::getCode
                ));

        List<ImportResult.RowError> errors = new ArrayList<>();

        List<HrEmpAppointment> rows;
        try {
            rows = EasyExcelFactory.read(file.getInputStream())
                    .head(HrEmpAppointment.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel 读取失败：" + e.getMessage());
        }

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("导入文件为空");
        }

        int successCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            HrEmpAppointment row = rows.get(i);

            // EasyExcel 数据行号通常从第 2 行开始，第一行为表头
            int excelRowIndex = i + 2;

            String userCode = trimToNull(row.getUserCode());
            String newChangeType = trimToNull(row.getChangeType());

            if (userCode == null) {
                errors.add(new ImportResult.RowError(
                        excelRowIndex,
                        null,
                        "员工档案号/工号不能为空"
                ));
                continue;
            }
            if (newChangeType == null) {
                errors.add(new ImportResult.RowError(
                        excelRowIndex,
                        userCode,
                        "导入行的 changeType 不能为空"
                ));
                continue;
            }

            List<HrEmpAppointment> existList = hrEmpAppointmentMapper.selectList(
                    Wrappers.<HrEmpAppointment>lambdaQuery()
                            .eq(HrEmpAppointment::getUserCode, userCode)
                            .in(HrEmpAppointment::getChangeType, LEAVE_CHANGE_TYPES)
                            .last("order by start_date desc nulls last, end_date desc limit 1")
            );

            if (existList == null || existList.isEmpty()) {
                errors.add(new ImportResult.RowError(
                        excelRowIndex,
                        userCode,
                        "未找到该员工的离职/退休/死亡任职记录"
                ));
                continue;
            }

//            if (existList.size() > 1) {
//                errors.add(new ImportResult.RowError(
//                        excelRowIndex,
//                        userCode,
//                        "找到多条离职/退休/死亡任职记录，请先清洗数据后再导入"
//                ));
//                continue;
//            }

            HrEmpAppointment target = existList.get(0);

            HrEmpAppointment updateEntity = new HrEmpAppointment();
            updateEntity.setId(target.getId());
            updateEntity.setChangeType(codeInfoMap.getOrDefault(newChangeType, newChangeType));

            int affected = hrEmpAppointmentMapper.updateById(updateEntity);

            if (affected != 1) {
                errors.add(new ImportResult.RowError(
                        excelRowIndex,
                        userCode,
                        "更新失败"
                ));
                continue;
            }

            successCount++;
        }

        ImportResult result = new ImportResult(rows.size(), successCount, errors);

        if (!errors.isEmpty()) {
            String errorMessage = errors.stream()
                    .map(e -> "第 " + e.getRowIndex() + " 行，员工：" + e.getUserCode() + "，原因：" + e.getMessage())
                    .collect(Collectors.joining("；"));

            throw new IllegalArgumentException("导入失败，已回滚。错误示例：" + errorMessage);
        }

        return result;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimValue = value.trim();
        return trimValue.isEmpty() ? null : trimValue;
    }

    public List<HrEmpAppointment> selectLatestMsg(List<String> userCodeList){

        if (userCodeList == null || userCodeList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = userCodeList.stream()
                .filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());

        MPJLambdaWrapper<HrEmpAppointment> w = new MPJLambdaWrapper<HrEmpAppointment>()
                // 关键：distinct on 每个 user_code 只保留一行
                .select("distinct on (t.user_code) t.*")
                .in(HrEmpAppointment::getUserCode, codes)
                // order by 顺序必须以 distinct on 的字段开头
                .orderByAsc("t.user_code")
                // Wrapper 不支持 NULLS LAST，用布尔表达式把 NULL 放到最后（false<true）
                .orderByAsc("(t.start_date is null)")
                .orderByDesc("t.start_date")
                .orderByDesc("t.id");  // 并列时间时用主键稳定排序
        List<HrEmpAppointment> hrEmpAppointments = hrEmpAppointmentMapper.selectList(w);
        // 回显码值，当数据为数字开头的时候才会进行转写，否则直接返回
        for(HrEmpAppointment entity : hrEmpAppointments) {
            String deptCode = entity.getDept();
            String deptName = DeptUtils.getDeptNameByCode(deptCode);
            entity.setDept(deptName);

            // 处理组织单元
            String orgId = entity.getOrgUnit();
            String orgName = DeptUtils.getDeptNameByCode(orgId);
            entity.setOrgUnit(orgName);

            // 处理组织
            String orgOriginal = entity.getOrg();
            if (StringUtils.isNotBlank(orgOriginal)) {
                String org = orgOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(orgOriginal, "hr_emp_appointment", "APPOINTMENT_ORG") : orgOriginal;
                entity.setOrg(org);
            }

            // 处理职位名称
            String position = entity.getPosition();
            if (StringUtils.isNotBlank(position)) {
                String positionName = position.matches("^\\d.*") ?
                        codeInfoService.translateCode(position, "hr_emp_appointment", "APPOINTMENT_POSITION") : position;
                entity.setPosition(positionName);
            }

            // 处理变动事件 changeEvent
            String changeEventOriginal = entity.getChangeEvent();
            if (StringUtils.isNotBlank(changeEventOriginal)) {
                String changeEvent = changeEventOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeEventOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_EVENT") : changeEventOriginal;
                entity.setChangeEvent(changeEvent);
            }

            // 处理变动类型 changeType
            String changeTypeOriginal = entity.getChangeType();
            if (StringUtils.isNotBlank(changeTypeOriginal)) {
                String changeType = changeTypeOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeTypeOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_TYPE") : changeTypeOriginal;
                entity.setChangeType(changeType);
            }

            // 处理变动原因 changeReason
            String changeReasonOriginal = entity.getChangeReason();
            if (StringUtils.isNotBlank(changeReasonOriginal)) {
                String changeReason = changeReasonOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(changeReasonOriginal, "hr_emp_appointment", "APPOINTMENT_CHANGE_REASON") : changeReasonOriginal;
                entity.setChangeReason(changeReason);
            }

            // 处理在岗状态 dutyStatus
            String dutyStatusOriginal = entity.getDutyStatus();
            if (StringUtils.isNotBlank(dutyStatusOriginal)) {
                String dutyStatus = dutyStatusOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(dutyStatusOriginal, "hr_emp_appointment", "APPOINTMENT_DUTY_STATUS") : dutyStatusOriginal;
                entity.setDutyStatus(dutyStatus);
            }

            // 处理职务类别 positionCategory
            String positionCategoryOriginal = entity.getPositionCategory();
            if (StringUtils.isNotBlank(positionCategoryOriginal)) {
                String positionCategory = positionCategoryOriginal.matches("^\\d.*") ?
                        codeInfoService.translateCode(positionCategoryOriginal, "hr_emp_appointment", "APPOINTMENT_POSITION_CATEGORY") : positionCategoryOriginal;
                entity.setPositionCategory(positionCategory);
            }

        }
        return hrEmpAppointments;
    }

    /**
     * 查询个人未结束的任职信息
     * @param userCode 员工档案号
     * @return
     */
    public List<HrEmpAppointment> selectUndoneApp(String userCode) {
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .eq(HrEmpAppointment::getUserCode, userCode)
                .and(w -> w
                    .isNull(HrEmpAppointment::getEndDate)
                    .or()
                    .gt(HrEmpAppointment::getEndDate, "2198-01-01")
                );
        List<HrEmpAppointment> result = hrEmpAppointmentMapper.selectList(wrapper);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }
}