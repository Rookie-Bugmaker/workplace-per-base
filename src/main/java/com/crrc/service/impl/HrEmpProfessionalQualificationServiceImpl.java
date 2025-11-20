package com.crrc.service.impl;

import com.crrc.entity.HrEmpProfessionalQualification;
import com.crrc.mapper.HrEmpProfessionalQualificationMapper;
import com.crrc.service.CodeInfoService;
import com.crrc.service.HrEmpProfessionalQualificationService;
import com.crrc.utils.SystemConstants;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 员工专业职务任职资格表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpProfessionalQualificationServiceImpl extends WorkServiceImpl<HrEmpProfessionalQualificationMapper, HrEmpProfessionalQualification> implements HrEmpProfessionalQualificationService {

    @Autowired
    private HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper;
    @Autowired
    private CodeInfoService codeInfoService;

    private static final String TABLE_NAME = "hr_emp_pro_qualification";

    /**
     * 查询用户的专业职务任职资格信息
     * @param userCode 需要查询的档案号
     * @return 查询结果
     */
    public List<HrEmpProfessionalQualification> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpProfessionalQualification> wrapper = new MPJLambdaWrapper<HrEmpProfessionalQualification>()
                .selectAll(HrEmpProfessionalQualification.class)
                .eq(HrEmpProfessionalQualification::getUserCode, userCode);
        List<HrEmpProfessionalQualification> hrEmpProfessionalQualifications = hrEmpProfessionalQualificationMapper.selectList(wrapper);
        if(!ObjectUtils.isEmpty(hrEmpProfessionalQualifications)) {
            // 拼装翻译Map
            Map<String, String> qidTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, "QUA_ID");
            Map<String, String> qlevelTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, "QUA_LEVEL");
            Map<String, String> qpathTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, "QUA_PATH");
            Map<String, String> booleanTranslateMap = new HashMap<>();
            booleanTranslateMap.put("1", "是");
            booleanTranslateMap.put("0", "否");
            // 翻译各个字段
            hrEmpProfessionalQualifications.forEach(qua -> {
                qua.setQuaIdValue(qidTranslateMap.getOrDefault(qua.getQuaId(), qua.getQuaId()));
                qua.setQuaTitleLevelValue(qlevelTranslateMap.getOrDefault(qua.getQuaTitleLevel(), qua.getQuaTitleLevel()));
                qua.setQuaPathValue(qpathTranslateMap.getOrDefault(qua.getQuaPath(), qua.getQuaPath()));
                qua.setIsHighestValue(booleanTranslateMap.getOrDefault(qua.getIsHighest(), qua.getIsHighest()));
                qua.setIsAppointedValue(booleanTranslateMap.getOrDefault(qua.getIsAppointed(), qua.getIsAppointed()));
            });

        }
        return hrEmpProfessionalQualifications;
    }

    public HrEmpProfessionalQualification selectHighestQualification(String userCode){
        MPJLambdaWrapper<HrEmpProfessionalQualification> wrapper = new MPJLambdaWrapper<HrEmpProfessionalQualification>()
                .selectAll(HrEmpProfessionalQualification.class)
                .eq(HrEmpProfessionalQualification::getUserCode, userCode)
                .eq(HrEmpProfessionalQualification::getIsHighest, SystemConstants.TRUE_FLAG);
        return hrEmpProfessionalQualificationMapper.selectOne(wrapper);
    }
}