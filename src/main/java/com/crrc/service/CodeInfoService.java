package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.CodeInfo;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2025-10-15 10:51:03
 */
public interface CodeInfoService extends IService<CodeInfo> {

    /**
     * 将指定的表中的指定字段进行字典值反写
     *
     * @param dictCode  字典key值
     * @param tableName 表名
     * @param type      字段名
     * @return 字典value值
     */
    String translateCode(String dictCode, String tableName, String type);
}