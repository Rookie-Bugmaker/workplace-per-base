package com.crrc.service.impl;

import com.crrc.entity.HrEmpFamMember;
import com.crrc.mapper.HrEmpFamMemberMapper;
import com.crrc.service.HrEmpFamMemberService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
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

    public List<HrEmpFamMember> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpFamMember> wrapper = new MPJLambdaWrapper<HrEmpFamMember>()
                .selectAll(HrEmpFamMember.class)
                .eq(HrEmpFamMember::getUserCode, userCode);
        return hrEmpFamMemberMapper.selectList(wrapper);
    }

}