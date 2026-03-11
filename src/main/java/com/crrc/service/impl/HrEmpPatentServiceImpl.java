package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPaper;
import com.crrc.entity.HrEmpPatent;
import com.crrc.mapper.HrEmpPaperMapper;
import com.crrc.mapper.HrEmpPatentMapper;
import com.crrc.service.HrEmpPatentService;
import com.crrc.utils.FileUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 专利信息表 服务实现类
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:36:49
 */
@Slf4j
@Service
public class HrEmpPatentServiceImpl extends WorkServiceImpl<HrEmpPatentMapper, HrEmpPatent> implements HrEmpPatentService {

    @Autowired
    private HrEmpPatentMapper hrEmpPatentMapper;

    @Override
    public List<HrEmpPatent> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpPatent> wrapper = new MPJLambdaWrapper<HrEmpPatent>()
                .selectAll(HrEmpPatent.class)
                .eq(HrEmpPatent::getUserCode, userCode)
                .orderByAsc(HrEmpPatent::getAuthorizationdate);
        return hrEmpPatentMapper.selectList(wrapper);
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(List<HrEmpPatent> hrEmpPatentList) {
        try {
            FileUtils.resolveFilesByHeaderId(hrEmpPatentList);
        } catch (Exception e) {
            log.warn("专利附件信息解析失败, {}", e.getMessage());
        }
    }
}