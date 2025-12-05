package com.crrc.utils;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author yukun
 */
public final class WrapperUtils {

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