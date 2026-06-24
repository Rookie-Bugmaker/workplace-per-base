package com.crrc.service.impl;

import com.crrc.entity.PositionDescription;
import com.crrc.mapper.PositionDescriptionMapper;
import com.crrc.service.PositionDescriptionService;
import org.springframework.stereotype.Service;

/**
 * 职位说明书 服务实现类。
 */
@Service
public class PositionDescriptionServiceImpl extends WorkServiceImpl<PositionDescriptionMapper, PositionDescription> implements PositionDescriptionService {
}
