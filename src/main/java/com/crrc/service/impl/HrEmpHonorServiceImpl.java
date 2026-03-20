package com.crrc.service.impl;

import com.crrc.entity.HrEmpHonor;
import com.crrc.entity.HrEmpPatent;
import com.crrc.mapper.HrEmpHonorMapper;
import com.crrc.mapper.HrEmpPatentMapper;
import com.crrc.service.HrEmpHonorService;
import com.crrc.utils.FileUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 荣誉信息表 服务实现类
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:38:02
 */
@Slf4j
@Service
public class HrEmpHonorServiceImpl extends WorkServiceImpl<HrEmpHonorMapper, HrEmpHonor> implements HrEmpHonorService {

    @Autowired
    private HrEmpHonorMapper hrEmpHonorMapper;

    @Override
    public List<HrEmpHonor> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpHonor> wrapper = new MPJLambdaWrapper<HrEmpHonor>()
                .selectAll(HrEmpHonor.class)
                .eq(HrEmpHonor::getUserCode, userCode)
                .orderByAsc(HrEmpHonor::getAwardDate);
        return hrEmpHonorMapper.selectList(wrapper);
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(List<HrEmpHonor> hrEmpHonorList) {
        try {
            FileUtils.resolveFilesByHeaderId(hrEmpHonorList);
        } catch (Exception e) {
            log.warn("荣誉附件信息解析失败, {}", e.getMessage());
        }
    }
}