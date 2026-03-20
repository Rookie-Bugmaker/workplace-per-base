package com.crrc.service.impl;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPaper;
import com.crrc.mapper.HrEmpPaperMapper;
import com.crrc.service.HrEmpPaperService;
import com.crrc.utils.FileUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 论文论著信息表 服务实现类
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:33:05
 */
@Service
@Slf4j
public class HrEmpPaperServiceImpl extends WorkServiceImpl<HrEmpPaperMapper, HrEmpPaper> implements HrEmpPaperService {

    @Autowired
    private HrEmpPaperMapper hrEmpPaperMapper;

    @Override
    public List<HrEmpPaper> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpPaper> wrapper = new MPJLambdaWrapper<HrEmpPaper>()
                .selectAll(HrEmpPaper.class)
                .eq(HrEmpPaper::getUserCode, userCode)
                .orderByAsc(HrEmpPaper::getPublishDate);
        return hrEmpPaperMapper.selectList(wrapper);
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(List<HrEmpPaper> hrEmpPaperList) {
        try {
            FileUtils.resolveFilesByHeaderId(hrEmpPaperList);
        } catch (Exception e) {
            log.warn("论文论著附件信息解析失败, {}", e.getMessage());
        }
    }

}