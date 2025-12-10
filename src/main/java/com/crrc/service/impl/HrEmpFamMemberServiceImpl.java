package com.crrc.service.impl;

import com.crrc.entity.HrEmpFamMember;
import com.crrc.mapper.HrEmpFamMemberMapper;
import com.crrc.service.CodeInfoService;
import com.crrc.service.HrEmpFamMemberService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工主要家庭成员社会关系表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
public class HrEmpFamMemberServiceImpl extends WorkServiceImpl<HrEmpFamMemberMapper, HrEmpFamMember> implements HrEmpFamMemberService {

    @Autowired
    private HrEmpFamMemberMapper hrEmpFamMemberMapper;
    @Autowired
    private CodeInfoService codeInfoService;

    public List<HrEmpFamMember> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpFamMember> wrapper = new MPJLambdaWrapper<HrEmpFamMember>()
                .selectAll(HrEmpFamMember.class)
                .eq(HrEmpFamMember::getUserCode, userCode);
        return hrEmpFamMemberMapper.selectList(wrapper);
    }


    /**
     * 处理家庭成员中的政治面貌，将码值转为汉字返回，如果处理失败就不会变更
     * @param hrEmpFamMember 等待处理的家庭成员的实体类
     */
    public void resolvePoliticalStatus(HrEmpFamMember hrEmpFamMember){
        // 处理政治面貌
        String famPoliticalStatus = codeInfoService.translateCode(hrEmpFamMember.getFamPoliticalStatus(), "hr_emp_fam_member", "FAM_POLITICAL_STATUS");
        if(StringUtils.isNotBlank(famPoliticalStatus)) {
            hrEmpFamMember.setFamPoliticalStatus(famPoliticalStatus);
        }
    }
}