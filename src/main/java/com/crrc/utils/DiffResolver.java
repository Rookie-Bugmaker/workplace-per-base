package com.crrc.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DiffResolver {

    /**
     * 通用差异比较方法
     *
     * @param originalData 原始数据列表
     * @param changedData  变更后的数据列表
     * @param clazz        实体类Class对象（用于获取字段）
     * @param <T>          必须实现 DiffMarkable 接口
     */
    public <T extends DiffMarkable> void resolveDifference(
            List<T> originalData,
            List<T> changedData,
            Class<T> clazz) {

        // 获取所有字段名
        Field[] fields = clazz.getDeclaredFields();

        // 构建原数据ID与索引的映射关系
        Map<String, Integer> originalIdMap = new HashMap<>();
        for (int i = 0; i < originalData.size(); i++) {
            T record = originalData.get(i);
            originalIdMap.put(record.getId(), i);
        }

        // 构建变更数据ID与索引的映射关系
        Map<String, Integer> changedIdMap = new HashMap<>();
        for (int i = 0; i < changedData.size(); i++) {
            T record = changedData.get(i);
            changedIdMap.put(record.getId(), i);
        }

        // 比较数据
        for (T changedRecord : changedData) {
            String changedId = changedRecord.getId();
            ArrayList<String> markList = new ArrayList<>();

            // 判断是否为新增记录
            if (changedId == null || changedId.isEmpty()) {
                markList.add("$NEW$");
            } else {
                // 判断是否为修改记录
                if (originalIdMap.containsKey(changedId)) {
                    T originalRecord = originalData.get(originalIdMap.get(changedId));

                    // 遍历字段，比较字段值是否一致
                    for (Field field : fields) {
                        field.setAccessible(true);
                        try {
                            Object originalValue = field.get(originalRecord);
                            Object changedValue = field.get(changedRecord);
                            if (originalValue == null && changedValue == null) {
                                continue;
                            }
                            if (originalValue == null || !originalValue.equals(changedValue)) {
                                markList.add(field.getName());
                            }
                        } catch (IllegalAccessException e) {
                            log.info(e.getMessage());
                        }
                    }
                } else {
                    // 如果原数据中不存在该ID，则标记为新增记录
                    markList.add("$NEW$");
                }
            }

            // 设置标记列表
            changedRecord.setMarkList(markList);
        }

        // 处理删除记录
        for (T originalRecord : originalData) {
            String originalId = originalRecord.getId();
            if (!changedIdMap.containsKey(originalId)) {
                // 如果变更数据中不存在该ID，则标记为删除记录
                ArrayList<String> markList = new ArrayList<>();
                markList.add("$DELETE$");
                originalRecord.setMarkList(markList);
                changedData.add(originalRecord);
            }
        }
    }
}