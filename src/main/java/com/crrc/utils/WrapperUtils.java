package com.crrc.utils;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author yukun
 */
public final class WrapperUtils {

    /**
     * 为实体类的所有字段生成带有前缀的 Select 语句
     *
     * @param wrapper     JoinWrapper 实例
     * @param entityClass 实体类 Class
     * @param tableAlias  SQL 中的表别名（例如 "t" 或 "t1"）
     * @param prefix      结果列的 AS 前缀（例如 "user_"）
     */
    public static void selectAllWithPrefix(MPJLambdaWrapper<?> wrapper, Class<?> entityClass, String tableAlias, String prefix) {
        // 通过 TableInfoHelper 获取 MyBatis Plus 解析好的表元数据
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            throw new IllegalArgumentException("未找到该实体的 TableInfo，请确保该类是受 MyBatis Plus 管理的实体");
        }

        List<String> selectColumns = new ArrayList<>();

        // 1. 获取并处理主键字段
        String keyColumn = tableInfo.getKeyColumn();
        if (StringUtils.isNotBlank(keyColumn)) {
            selectColumns.add(String.format("%s.%s AS %s%s", tableAlias, keyColumn, prefix, keyColumn));
        }

        // 2. 获取并处理普通字段
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            // 拼接格式: t1.column_name AS prefix_column_name
            selectColumns.add(String.format("%s.%s AS %s%s", tableAlias, fieldInfo.getColumn(), prefix, fieldInfo.getColumn()));
        }

        // 3. 将拼接好的字段集加入到 wrapper 的 select 中
        wrapper.select(String.join(", ", selectColumns));
    }

    /** 构建 QueryWrapper：字符串默认用 eq，非字符串用 eq */
    public static <T> QueryWrapper<T> buildWrapper(T entity) {
        return buildWrapper(entity, StringMode.EQ);
    }

    /** 构建 QueryWrapper：可选字符串策略（EQ / LIKE / LIKE_LEFT / LIKE_RIGHT） */
    public static <T> QueryWrapper<T> buildWrapper(T entity, StringMode stringMode) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (entity == null) {
            return wrapper;
        }

        Class<?> clazz = entity.getClass();
        for (Field field : getAllFields(clazz)) {
            if (shouldSkip(field)) {
                continue;
            }

            field.setAccessible(true);
            Object value;
            try {
                value = field.get(entity);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (isEmpty(value)) {
                continue;
            }

            // 解析列名
            String column = resolveColumnName(field);

            // 字符串用指定策略，其它类型 eq
            if (value instanceof CharSequence) {
                String str = value.toString().trim();
                if (str.isEmpty()) {
                    continue;
                }
                switch (stringMode) {
                    case LIKE:
                        wrapper.like(column, str);
                        break;
                    case LIKE_LEFT:
                        wrapper.likeLeft(column, str);
                        break;
                    case LIKE_RIGHT:
                        wrapper.likeRight(column, str);
                        break;
                    default:
                        wrapper.eq(column, str);
                        break;
                }
            } else {
                wrapper.eq(column, value);
            }
        }
        return wrapper;
    }

    public enum StringMode {
        // 等于 column = 'str'
        EQ,
        // 全模糊，匹配任意位置 column LIKE '%str%'
        LIKE,
        // 左模糊，仅匹配开头 column LIKE 'str%'
        LIKE_LEFT,
        // 右模糊，匹配结尾 column LIKE '%str'
        LIKE_RIGHT
    }

    // --------- 工具方法 ---------

    private static boolean shouldSkip(Field f) {
        int m = f.getModifiers();
        if (Modifier.isStatic(m) || Modifier.isTransient(m) || "serialVersionUID".equals(f.getName())) {
            return true;
        }

        // 若 @TableField(exist=false) 则跳过
        TableField tf = f.getAnnotation(TableField.class);
        if (tf != null && !tf.exist()) {
            return true;
        }

        return f.isSynthetic();
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private static String resolveColumnName(Field f) {
        TableField tf = f.getAnnotation(TableField.class);
        if (tf != null && StringUtils.isNotBlank(tf.value())) {
            return tf.value();
        }
        // 无注解时：驼峰转下划线
        return camelToUnderline(f.getName());
    }

    private static String camelToUnderline(String name) {
        StringBuilder sb = new StringBuilder();
        for (char ch : name.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                sb.append('_').append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** 判空：null、空字符串、空集合/Map、空数组、空 Optional 都视为空 */
    private static boolean isEmpty(Object v) {
        if (v == null) {
            return true;
        }
        if (v instanceof CharSequence) {
            return ((CharSequence) v).toString().trim().isEmpty();
        }
        if (v instanceof Collection<?>) {
            return ((Collection<?>) v).isEmpty();
        }
        if (v instanceof Map<?, ?>) {
            return ((Map<?, ?>) v).isEmpty();
        }
        if (v.getClass().isArray()) {
            return Array.getLength(v) == 0;
        }
        if (v instanceof Optional<?>) {
            return ((Optional<?>) v).isPresent();
        }
        return false;
    }
}