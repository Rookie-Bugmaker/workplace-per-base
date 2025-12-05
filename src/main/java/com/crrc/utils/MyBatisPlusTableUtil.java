package com.crrc.utils;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyBatisPlusTableUtil {
    //需要排除的字段
    private static final List<String> EXCLUDE_COLUMNS = Arrays.asList("tenant_id","validate_key", "file_header_id", "profile_picture", "attendance_image_id");

    /**
     * 获取实体类对应的表名
     */
    public String getTableName(Class<?> entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        return tableInfo != null ? tableInfo.getTableName() : null;
    }

    /**
     * 获取实体类对应的所有数据库列名
     */
    public List<String> getTableColumns(Class<?> entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            throw new IllegalArgumentException("无法获取表信息: " + entityClass.getName());
        }

        return tableInfo.getFieldList().stream()
                .map(field -> field.getColumn())
                .filter(column -> !EXCLUDE_COLUMNS.contains(column)) // 过滤掉排除的列
                .collect(Collectors.toList());
    }

    /**
     * 获取主键列名
     */
    public String getPrimaryKey(Class<?> entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        return tableInfo != null ? tableInfo.getKeyColumn() : null;
    }
}