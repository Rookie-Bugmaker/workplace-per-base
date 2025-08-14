package com.crrc.utils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UpdateWrapperUtils {

    public static <T> UpdateWrapper<T> buildNonNullUpdateWrapper(T entity, String... excludeFields) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        if (entity == null) {
            return wrapper;
        }

        Set<String> excludes = new HashSet<>(Arrays.asList(excludeFields));

        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(entity) != null && !excludes.contains(field.getName());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(field -> {
                    try {
                        String columnName = camelToUnderline(field.getName());
                        wrapper.set(columnName, field.get(entity));
                    } catch (Exception ignored) {}
                });

        return wrapper;
    }

    private static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}