package com.crrc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtils {

    /**
     * 将 list 按固定大小分片
     *
     * @param list  原始列表
     * @param batchSize  每个分片的大小（> 0）
     * @param <T>   元素类型
     * @return      分片后的列表，比如 List<List<T>>
     */
    public static <T> List<List<T>> partition(List<T> list, int batchSize) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }

        int size = list.size();
        List<List<T>> result = new ArrayList<>((size + batchSize - 1) / batchSize);

        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(size, i + batchSize);
            // 关键：这里用 new ArrayList 拷贝一份，避免 subList 的各种坑
            result.add(new ArrayList<>(list.subList(i, end)));
        }
        return result;
    }
}