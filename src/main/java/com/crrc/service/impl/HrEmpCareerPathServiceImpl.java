package com.crrc.service.impl;

import com.crrc.entity.HrEmpCareerPath;
import com.crrc.mapper.HrEmpCareerPathMapper;
import com.crrc.service.HrEmpCareerPathService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职业发展通道 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpCareerPathServiceImpl extends WorkServiceImpl<HrEmpCareerPathMapper, HrEmpCareerPath> implements HrEmpCareerPathService {
    @Autowired
    private HrEmpCareerPathMapper hrEmpCareerPathMapper;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    public List<HrEmpCareerPath> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpCareerPath> wrapper = new MPJLambdaWrapper<HrEmpCareerPath>()
                .selectAll(HrEmpCareerPath.class)
                .eq(HrEmpCareerPath::getUserCode, userCode)
                .orderByAsc(HrEmpCareerPath::getQualificationDate);
        List<HrEmpCareerPath> hrEmpCareerPaths = hrEmpCareerPathMapper.selectList(wrapper);

        return hrEmpCareerPaths;
    }

    @Override
    public void resolveDetails(List<HrEmpCareerPath> hrEmpCareerPaths) {
        Map<String, String> translateMap = codeInfoService.getTranslateMap("hr_emp_career_path", "CAREER_PATH_ID");
        for(HrEmpCareerPath path : hrEmpCareerPaths) {
            // 处理过期时间
            if(path.getExpiryDate() != null && path.getExpiryDate().isAfter(LocalDate.parse("2198-01-01"))) {
                path.setExpiryDate(null);
            }
            // 翻译是否生效
            path.setIsExpiredValue("1".equals(path.getIsExpired()) ? "失效" : "生效");

            // 翻译职业发展通道
            path.setPathId(translateMap.getOrDefault(path.getPathId(), path.getPathId()));
        }
    }
}