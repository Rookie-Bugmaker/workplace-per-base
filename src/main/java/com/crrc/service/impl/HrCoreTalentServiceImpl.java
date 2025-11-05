package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.HrCoreTalent;
import com.crrc.mapper.HrCoreTalentMapper;
import com.crrc.service.HrCoreTalentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class HrCoreTalentServiceImpl extends WorkServiceImpl<HrCoreTalentMapper, HrCoreTalent> implements HrCoreTalentService {
    public void queryVO(Page<HrCoreTalent> page, HrCoreTalent pageReqVO) throws ParseException {

        initDateTimeQuery(pageReqVO);

        this.query(page, pageReqVO);

        buildDateTimeStr(page.getRecords());
    }

    public List<HrCoreTalent> queryList(HrCoreTalent queryReqVO) throws ParseException {
        initDateTimeQuery(queryReqVO);

        List<HrCoreTalent> records = this.query(queryReqVO);

        buildDateTimeStr(records);

        return records;
    }

    private void initDateTimeQuery(HrCoreTalent reqVO) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(StringUtils.hasText(reqVO.getAppointmentStartDateStr())) {
            reqVO.setAppointmentStartDate(simpleDateFormat.parse(reqVO.getAppointmentStartDateStr()));
        }

        if(StringUtils.hasText(reqVO.getAppointmentEndDateStr())) {
            reqVO.setAppointmentEndDate(simpleDateFormat.parse(reqVO.getAppointmentEndDateStr()));
        }
    }

    private void buildDateTimeStr(List<HrCoreTalent> records) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        records.forEach(item -> {
            if(item.getAppointmentStartDate() != null) {
                item.setAppointmentStartDateStr(simpleDateFormat.format(item.getAppointmentStartDate()));
            }

            if(item.getAppointmentEndDate() != null) {
                item.setAppointmentEndDateStr(simpleDateFormat.format(item.getAppointmentEndDate()));
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveVO(@Valid HrCoreTalent createReqVO) throws ParseException {

        initDateTimeQuery(createReqVO);

        this.save(createReqVO);

        return true;
    }

    public Boolean saveOrUpdateVO(@Valid HrCoreTalent updateReqVO) throws ParseException {
        initDateTimeQuery(updateReqVO);

        this.saveOrUpdate(updateReqVO);

        return true;
    }
}
