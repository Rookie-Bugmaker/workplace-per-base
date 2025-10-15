package com.crrc.utils;

import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于封装一些独特的不一定别的类能复用的私有方法
 */
public class MyUtils {

    /**
     * 用于分割回传的主键
     * @param bizId 业务主键
     * @return
     */
    public static String subPk(String bizId) {
        // 找到下划线的位置
        int underscoreIndex = bizId.indexOf('_');
        // 从开始到下划线前
        String type = bizId.substring(0, underscoreIndex);
        // 从下划线后到结尾
        return bizId.substring(underscoreIndex + 1);
    }

    /**
     * 辅助方法：将数据对象转换为Map（可根据实际需求调整）
     * @param data 等待转换的数据
     * @return 转换为map的数据
     */
    public static List<Map<String, Object>> convertToMapList(Object data) {
        // 方案1：如果数据本身就是集合
        if (data instanceof Collection) {
            return ((Collection<?>) data).stream()
                    // 改为类名引用静态方法
                    .map(MyUtils::convertSingleObjectToMap)
                    .collect(Collectors.toList());
        }

        // 方案2：如果是单个对象
        return Collections.singletonList(convertSingleObjectToMap(data));
    }

    /**
     * 将单个对象转为Map
     * @param obj 转换的单个对象
     * @return map
     */
    public static Map<String, Object> convertSingleObjectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }

        try {
            return Arrays.stream(BeanUtils.getPropertyDescriptors(obj.getClass()))
                    .filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName()))
                    .collect(HashMap::new,
                            (map, pd) -> {
                                try {
                                    Object value = pd.getReadMethod().invoke(obj);
                                    if (value != null) {  // 显式过滤null值
                                        map.put(pd.getName(), value);
                                    }
                                } catch (Exception ignored) {}
                            },
                            HashMap::putAll);
        } catch (Exception e) {
            throw new RuntimeException("对象转换Map失败", e);
        }
    }
}
