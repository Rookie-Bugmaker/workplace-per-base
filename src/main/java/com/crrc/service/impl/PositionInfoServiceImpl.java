package com.crrc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.service.PositionInfoService;
import com.crrc.utils.ConstantsUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PositionInfoServiceImpl extends WorkServiceImpl<PositionInfoMapper, PositionInfo> implements PositionInfoService {
    private final PostCatalogInfoServiceImpl postCatalogInfoService;

    private static final String POSITION_LEVEL_TYPE = "POSITION_LEVEL";
    private static final String POSITION_INFO_TABLE_NAME = "position_info";
    private static final String POSITION_LEVEL_TYPE_NAME = "职层";
    private static final Long TENANT_ID = 438L;

    private final CodeInfoServiceImpl codeInfoService;

    public PositionInfoServiceImpl(
            PostCatalogInfoServiceImpl postCatalogInfoService,
            CodeInfoServiceImpl codeInfoService
    ) {
        this.postCatalogInfoService = postCatalogInfoService;
        this.codeInfoService = codeInfoService;
    }

    @Override
    public void translateField(List<PositionInfo> records) {
        // 使用util获得审批状态翻译的map
        Map<String, String> auditStatusTranslateMap = ConstantsUtils.valueToDescMap(AuditConstants.class);
        // 查询出全部的职群，职类、职种数据，并收集为map
        Map<String, String> jobCatalogTranslateMap = postCatalogInfoService.query(new PostCatalogInfo()).stream().collect(Collectors.toMap(
                PostCatalogInfo::getCode,
                PostCatalogInfo::getName
        ));
        // 获得职层翻译的字典
        Map<String, String> positionLevelTranslateMap = codeInfoService.getTranslateMap(POSITION_INFO_TABLE_NAME, POSITION_LEVEL_TYPE);
        records.forEach(item -> {
            // 获取职群信息(去掉最后一个-之后的内容)
            String jobGroup = item.getJobType().replaceAll("-[^-]*$", "");
            // 获取职类信息(去掉前一个基础上，最后一个-之后的内容)
            String jobCategory = jobGroup.replaceAll("-[^-]*$", "");
            item.setAuditStatusValue(auditStatusTranslateMap.getOrDefault(item.getAuditStatus(), item.getAuditStatus()));
            item.setJobTypeValue(jobCatalogTranslateMap.getOrDefault(item.getJobType(), item.getJobType()));
            item.setJobGroupValue(jobCatalogTranslateMap.getOrDefault(jobGroup, jobGroup));
            item.setJobCategoryValue(jobCatalogTranslateMap.getOrDefault(jobCategory, jobCategory));
            // 翻译职层
            item.setPositionLevelValue(positionLevelTranslateMap.getOrDefault(item.getPositionLevel(), item.getPositionLevel()));
        });
    }

    @Override
    public void queryActivated(Page<PositionInfo> page, PositionInfo pageReqVO) {
        pageReqVO.setAuditStatus(AuditConstants.STATUS_AUDIT_PASS);
        this.query(page, pageReqVO);
    }

    @Override
    public List<CodeInfo> queryPositionLevel() {
        CodeInfo codeInfo = new CodeInfo();
        codeInfo.setType(POSITION_LEVEL_TYPE);
        codeInfo.setTableName(POSITION_INFO_TABLE_NAME);
        return codeInfoService.query(codeInfo);
    }

    @Override
    public Boolean saveOrUpdatePositionLevel(CodeInfo codeInfo) {
        // 设置type与tableName信息
        codeInfo.setType(POSITION_LEVEL_TYPE);
        codeInfo.setTypeName(POSITION_LEVEL_TYPE_NAME);
        codeInfo.setTableName(POSITION_INFO_TABLE_NAME);
        // id需要手动生成
        codeInfo.setId(IdUtil.randomUUID());
        // 手动设置租户id
        codeInfo.setTenantId(TENANT_ID);
        return codeInfoService.saveOrUpdate(codeInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removePositionLevel(List<String> ids) {
        return codeInfoService.removeBatchByIds(ids);
    }
}
