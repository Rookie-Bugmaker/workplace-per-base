package com.crrc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtils {

    /**
     * 将 list 按固定大小分片
     *
     * @param list  原始列表
     * @param size  每个分片的大小（> 0）
     * @param <T>   元素类型
     * @return      分片后的列表，比如 List<List<T>>
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        List<List<T>> result = new ArrayList<>();
        int total = list.size();
        for (int i = 0; i < total; i += size) {
            int end = Math.min(total, i + size);
            // 注意 subList 是视图，如需独立 List，可以再 new ArrayList<>(...)
            result.add(new ArrayList<>(list.subList(i, end)));
        }
        return result;
    }
}