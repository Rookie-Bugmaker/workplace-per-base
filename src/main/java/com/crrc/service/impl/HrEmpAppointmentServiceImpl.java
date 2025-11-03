package com.crrc.service.impl;

import com.crrc.entity.HrEmpAppointment;
import com.crrc.mapper.HrEmpAppointmentMapper;
import com.crrc.service.HrEmpAppointmentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    public List<HrEmpAppointment> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .selectAll(HrEmpAppointment.class)
                .eq(HrEmpAppointment::getUserCode, userCode)
                .orderByAsc(HrEmpAppointment::getStartDate);
        return hrEmpAppointmentMapper.selectList(wrapper);
    }

    public List<HrEmpAppointment> selectLatestMsg(List<String> userCodeList){

        if (userCodeList == null || userCodeList.isEmpty()) return Collections.emptyList();
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

        return hrEmpAppointmentMapper.selectList(w);

    }

    /**
     * 查询个人未结束的任职信息
     * @param userCode 员工档案号
     * @return
     */
    public List<HrEmpAppointment> selectUndoneApp(String userCode) {
        MPJLambdaWrapper<HrEmpAppointment> wrapper = new MPJLambdaWrapper<HrEmpAppointment>()
                .eq(HrEmpAppointment::getUserCode, userCode)
                .isNull(HrEmpAppointment::getEndDate);
        List<HrEmpAppointment> result = hrEmpAppointmentMapper.selectList(wrapper);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }
}