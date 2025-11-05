package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.HrCoreTalent;
import com.crrc.mapper.HrCoreTalentMapper;
import com.crrc.service.HrCoreTalentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Service
public class HrCoreTalentServiceImpl extends WorkServiceImpl<HrCoreTalentMapper, HrCoreTalent> implements HrCoreTalentService {
    public void queryVO(Page<HrCoreTalent> page, HrCoreTalent pageReqVO) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(StringUtils.hasText(pageReqVO.getAppointmentStartDateStr())) {
            pageReqVO.setAppointmentStartDate(simpleDateFormat.parse(pageReqVO.getAppointmentStartDateStr()));
        }

        if(StringUtils.hasText(pageReqVO.getAppointmentEndDateStr())) {
            pageReqVO.setAppointmentEndDate(simpleDateFormat.parse(pageReqVO.getAppointmentEndDateStr()));
        }

        this.query(page, pageReqVO);

        page.getRecords().forEach(item -> {
            if(item.getAppointmentStartDate() != null) {
                item.setAppointmentStartDateStr(simpleDateFormat.format(item.getAppointmentStartDate()));
            }

            if(item.getAppointmentEndDate() != null) {
                item.setAppointmentEndDateStr(simpleDateFormat.format(item.getAppointmentEndDate()));
            }

        });
    }
}
