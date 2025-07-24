package com.crrc.mapper;

import com.crrc.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* <p>
    * 日志表 Mapper 接口
    * </p>
*
* @author mayiyao
* @since 2025-07-24 09:07:55
*/
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

}