package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.service.PostCatalogInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yukun
 */
@Service
@Slf4j
public class PostCatalogInfoServiceImpl extends WorkServiceImpl<PostCatalogInfoMapper, PostCatalogInfo> implements PostCatalogInfoService {

}
