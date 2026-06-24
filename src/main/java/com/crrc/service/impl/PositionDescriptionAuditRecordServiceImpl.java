package com.crrc.service.impl;

import com.crrc.entity.PositionDescriptionAuditRecord;
import com.crrc.mapper.PositionDescriptionAuditRecordMapper;
import com.crrc.service.PositionDescriptionAuditRecordService;
import org.springframework.stereotype.Service;

/**
 * 职位说明书审批记录 服务实现类。
 */
@Service
public class PositionDescriptionAuditRecordServiceImpl extends WorkServiceImpl<PositionDescriptionAuditRecordMapper, PositionDescriptionAuditRecord> implements PositionDescriptionAuditRecordService {
}
