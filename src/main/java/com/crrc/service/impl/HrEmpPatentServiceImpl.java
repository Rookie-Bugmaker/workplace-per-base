package com.crrc.service.impl;

import com.alibaba.fastjson.JSON;
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
        // 1 打印入参
        log.info("查询专利信息开始，userCode={}", userCode);
        MPJLambdaWrapper<HrEmpPatent> wrapper = new MPJLambdaWrapper<HrEmpPatent>()
                .selectAll(HrEmpPatent.class)
                .eq(HrEmpPatent::getUserCode, userCode)
                .orderByAsc(HrEmpPatent::getAuthorizationDate);
        // 2 打印查询条件
        log.info("查询条件 userCode = {}", userCode);
        // 3 执行查询
        List<HrEmpPatent> result = hrEmpPatentMapper.selectList(wrapper);

        // 4 打印结果数量
        if(result == null || result.isEmpty()){
            log.warn("未查询到专利数据，userCode={}", userCode);
        }else{
            log.info("查询到专利数据条数：{}", result.size());
            log.info("专利数据内容：{}", JSON.toJSONString(result));
        }

        return result;
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