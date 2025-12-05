package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.HrCoreTalent;
import com.crrc.entity.HrEmpBasic;
import com.crrc.mapper.HrCoreTalentMapper;
import com.crrc.service.HrCoreTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class HrCoreTalentServiceImpl extends WorkServiceImpl<HrCoreTalentMapper, HrCoreTalent> implements HrCoreTalentService {
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;

    private static final String TABLE_NAME = "hr_core_talent";

    private static final String SEQ_TYPE_NAME = "SEQUENCE_TYPE";

    private static final String TALENT_TYPE_NAME = "TALENT_LEVEL";

    private static final String APPOINTMENT_STATUS_TYPE_NAME = "APPOINTMENT_STATUS";

    public void queryVO(Page<HrCoreTalent> page, HrCoreTalent pageReqVO) throws ParseException {
        // 如果有DateStr没有Date，则拼装Date
        initDateTimeQuery(pageReqVO);

        this.query(page, pageReqVO);
        // 翻译时间
        buildDateTimeStr(page.getRecords());
        // 翻译码值
        translateCode(page.getRecords());
    }

    public void translateCode(List<HrCoreTalent> records) {
        Map<String, String> talentTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, TALENT_TYPE_NAME);
        Map<String, String> seqTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, SEQ_TYPE_NAME);
        Map<String, String> appTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, APPOINTMENT_STATUS_TYPE_NAME);
        records.forEach(item -> {
            item.setSequenceType(seqTranslateMap.getOrDefault(item.getSequenceType(), item.getSequenceType()));
            item.setTalentLevel(talentTranslateMap.getOrDefault(item.getTalentLevel(), item.getTalentLevel()));
            item.setAppointmentStatus(appTranslateMap.getOrDefault(item.getAppointmentStatus(), item.getAppointmentStatus()));
            // 联表查询姓名
            if(org.apache.commons.lang3.StringUtils.isNotBlank(item.getJobNumber())) {
                HrEmpBasic hrEmpBasic = hrEmpBasicService.getByUserCode(item.getJobNumber());
                if(hrEmpBasic != null) {
                    item.setName(hrEmpBasic.getName());
                }
            }
        });
    }

    public List<HrCoreTalent> queryList(HrCoreTalent queryReqVO) throws ParseException {
        // 如果有DateStr没有Date，则拼装Date
        initDateTimeQuery(queryReqVO);

        List<HrCoreTalent> records = this.query(queryReqVO);
        // 翻译时间
        buildDateTimeStr(records);

        // 翻译码值
        translateCode(records);

        return records;
    }

    private void initDateTimeQuery(HrCoreTalent reqVO) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(StringUtils.hasText(reqVO.getAppointmentStartDateStr()) && reqVO.getAppointmentStartDate() == null) {
            reqVO.setAppointmentStartDate(simpleDateFormat.parse(reqVO.getAppointmentStartDateStr()));
        }

        if(StringUtils.hasText(reqVO.getAppointmentEndDateStr())  && reqVO.getAppointmentEndDate() == null) {
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
        // 如果有DateStr没有Date，则拼装Date
        initDateTimeQuery(createReqVO);
        this.save(createReqVO);

        return true;
    }

    public Boolean saveOrUpdateVO(@Valid HrCoreTalent updateReqVO) throws ParseException {
        initDateTimeQuery(updateReqVO);

        this.saveOrUpdate(updateReqVO);

        return true;
    }

    public HrCoreTalent getEntityById(Long id) {
        HrCoreTalent talent = this.getById(id);
        buildDateTimeStr(Collections.singletonList(talent));
        translateCode(Collections.singletonList(talent));
        return talent;
    }
}
