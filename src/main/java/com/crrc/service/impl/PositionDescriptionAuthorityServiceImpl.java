package com.crrc.service.impl;

import com.crrc.entity.PositionDescriptionAuthority;
import com.crrc.mapper.PositionDescriptionAuthorityMapper;
import com.crrc.service.PositionDescriptionAuthorityService;
import org.springframework.stereotype.Service;

/**
 * 职位说明书-工作权限 服务实现类。
 */
@Service
public class PositionDescriptionAuthorityServiceImpl extends WorkServiceImpl<PositionDescriptionAuthorityMapper, PositionDescriptionAuthority> implements PositionDescriptionAuthorityService {
}
