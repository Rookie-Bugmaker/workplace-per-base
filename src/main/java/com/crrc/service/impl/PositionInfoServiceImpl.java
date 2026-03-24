package com.crrc.service.impl;

import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.pojo.vo.PostCatalogInfoVO;
import com.crrc.service.PositionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


        records.forEach(position -> {
            String[] split = position.getJobType().split("-");
            if(split.length == 3) {
                position.setJobCategoryValue( translateMap.getOrDefault(split[0], split[0]));
                position.setJobGroupValue(translateMap.getOrDefault(String.join("-", split[0], split[1]), String.join("-", split[0], split[1])));
                position.setJobTypeValue(translateMap.getOrDefault(position.getJobType(), position.getJobType()));
            }
        });
    }
}
