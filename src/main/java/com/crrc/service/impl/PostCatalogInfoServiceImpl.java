package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.service.PostCatalogInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yukun
 */
@Service
@Slf4j
public class PostCatalogInfoServiceImpl extends WorkServiceImpl<PostCatalogInfoMapper, PostCatalogInfo> implements PostCatalogInfoService {
    public PostCatalogInfo getByTypeName(String typeName) {
        LambdaQueryWrapper<PostCatalogInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostCatalogInfo::getTypeName, typeName);
        return this.baseMapper.selectOne(queryWrapper);
    }
}
