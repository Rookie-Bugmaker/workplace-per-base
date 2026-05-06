package com.crrc.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.service.PositionInfoService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.PositionInfoImportListener;
import com.crrc.utils.WrapperUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PositionInfoServiceImpl extends WorkServiceImpl<PositionInfoMapper, PositionInfo> implements PositionInfoService {

    @Autowired
    PostCatalogInfoServiceImpl postCatalogInfoService;

    @Override
    public void resolveField(List<PositionInfo> records) {
        Map<String, String> translateMap = postCatalogInfoService.query(new PostCatalogInfo()).stream().collect(Collectors.toMap(
                PostCatalogInfo::getCode,
                PostCatalogInfo::getName,
                (a, b) -> a
        ));

        Map<String, String> deptTranslateMap = DeptUtils.getDeptTranslateMap();


        records.forEach(position -> {
            String[] split = position.getJobType().split("-");
            if(split.length == 3) {
                position.setJobCategoryValue( translateMap.getOrDefault(split[0], split[0]));
                position.setJobGroupValue(translateMap.getOrDefault(String.join("-", split[0], split[1]), String.join("-", split[0], split[1])));
                position.setJobTypeValue(translateMap.getOrDefault(position.getJobType(), position.getJobType()));
            }

            position.setOrgUnitValue(deptTranslateMap.getOrDefault(position.getOrgUnit(), position.getOrgUnit()));
        });
    }

    @Override
    public void queryByOrder(Page<PositionInfo> page, PositionInfo pageReqVO) {
        QueryWrapper<PositionInfo> wrapper = WrapperUtils.buildWrapper(pageReqVO, WrapperUtils.StringMode.LIKE);

        wrapper.eq(StringUtils.isNotEmpty(pageReqVO.getOrgUnit()), "org_unit", pageReqVO.getOrgUnit());
        wrapper.last("order by update_time desc nulls last");
        baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PositionInfo> selectByCodes(List<String> codes) {
        return baseMapper.selectList(new QueryWrapper<PositionInfo>().in("code", codes));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importJobGrade(MultipartFile file) throws Exception {
        EasyExcelFactory.read(file.getInputStream(), PositionInfo.class,
                        new PositionInfoImportListener(this))
                .headRowNumber(1)
                .sheet()
                .doRead();
    }
}
