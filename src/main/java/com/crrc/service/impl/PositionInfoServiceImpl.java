package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.pojo.dto.SysDeptPersonDTO;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.service.PositionInfoService;
import com.crrc.utils.ConstantsUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PositionInfoServiceImpl extends WorkServiceImpl<PositionInfoMapper, PositionInfo> implements PositionInfoService {
    private final SysServerClient sysServerClient;
    private final HrEmpBasicMapper hrEmpBasicMapper;
    private final PostCatalogInfoServiceImpl postCatalogInfoService;

    private static final String POSITION_LEVEL_TYPE_NAME = "POSITION_LEVEL";
    private static final String POSITION_INFO_TABLE_NAME = "position_info";
    private final CodeInfoServiceImpl codeInfoService;

    public PositionInfoServiceImpl(SysServerClient sysServerClient,
                                   HrEmpBasicMapper hrEmpBasicMapper,
                                   PostCatalogInfoServiceImpl postCatalogInfoService, CodeInfoServiceImpl codeInfoService) {
        this.sysServerClient = sysServerClient;
        this.hrEmpBasicMapper = hrEmpBasicMapper;
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
        records.forEach(item -> {
            // 获取职群信息(去掉最后一个-之后的内容)
            String jobGroup = item.getJobType().replaceAll("-[^-]*$", "");
            // 获取职类信息(去掉前一个基础上，最后一个-之后的内容)
            String jobCategory = jobGroup.replaceAll("-[^-]*$", "");
            item.setAuditStatusValue(auditStatusTranslateMap.getOrDefault(item.getAuditStatus(), item.getAuditStatus()));
            item.setJobTypeValue(jobCatalogTranslateMap.getOrDefault(item.getJobType(), item.getJobType()));
            item.setJobGroupValue(jobCatalogTranslateMap.getOrDefault(jobGroup, jobGroup));
            item.setJobCategoryValue(jobCatalogTranslateMap.getOrDefault(jobCategory, jobCategory));
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
        codeInfo.setType(POSITION_LEVEL_TYPE_NAME);
        codeInfo.setTableName(POSITION_INFO_TABLE_NAME);
        return codeInfoService.query(codeInfo);
    }

    @Override
    public Boolean saveOrUpdatePositionLevel(CodeInfo codeInfo) {
        // 设置type与tableName信息
        codeInfo.setType(POSITION_LEVEL_TYPE_NAME);
        codeInfo.setTableName(POSITION_INFO_TABLE_NAME);
        return codeInfoService.saveOrUpdate(codeInfo);
    }

    // TODO: 这个方法没有用上
    private SysUserDTO findUserByUserCode(String userCode) {
        ResultData<SysDeptPersonDTO> resultData = sysServerClient.findByUserCode(userCode);
        if (Boolean.FALSE.equals(resultData.getSuccess())) {
            throw new IllegalArgumentException("人员查询失败");
        }
        SysDeptPersonDTO deptPerson = resultData.getResult();
        if (ObjectUtils.isEmpty(deptPerson)) {
            throw new IllegalArgumentException("人员不存在");
        }

        ResultData<List<SysUserDTO>> sysUserResultData = sysServerClient.findByPersonIds(Collections.singletonList(deptPerson.getPersonId()));

        if (Boolean.FALSE.equals(sysUserResultData.getSuccess())) {
            throw new IllegalArgumentException("用户查询失败");
        }
        List<SysUserDTO> sysUserList = sysUserResultData.getResult();
        if(CollectionUtils.isEmpty(sysUserList)){
            throw new IllegalArgumentException("未查询到相关用户");
        }
        SysUserDTO sysUserDTO = sysUserList.get(0);
        if(sysUserDTO.getFullName() == null) {
            LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HrEmpBasic::getUserCode, userCode);
            List<HrEmpBasic> hrEmpBasicList = hrEmpBasicMapper.selectList(wrapper);
            if(!ObjectUtils.isEmpty(hrEmpBasicList)) {
                sysUserDTO.setFullName(hrEmpBasicList.get(0).getName());
            }
        }
        return sysUserDTO;
    }
}
