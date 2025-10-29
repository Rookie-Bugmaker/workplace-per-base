package com.crrc.service.impl;

import com.crrc.entity.PositionInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.service.PositionInfoService;
import org.springframework.stereotype.Service;

@Service
public class PositionInfoServiceImpl extends WorkServiceImpl<PositionInfoMapper, PositionInfo> implements PositionInfoService {
}
