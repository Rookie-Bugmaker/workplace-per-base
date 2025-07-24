package com.crrc.service.impl;

import com.crrc.service.impl.WorkServiceImpl;
import com.crrc.entity.AuditLog;
import com.crrc.mapper.AuditLogMapper;
import com.crrc.service.AuditLogService;
import org.springframework.stereotype.Service;

/**
* <p>
    * 日志表 服务实现类
    * </p>
*
* @author mayiyao
* @since 2025-07-24 09:07:55
*/
@Service
public class AuditLogServiceImpl extends WorkServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

}