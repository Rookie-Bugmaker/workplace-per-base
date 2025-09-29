package com.crrc.utils;

public class MyUtils {

    public static String subPk(String bizId) {
        // 找到下划线的位置
        int underscoreIndex = bizId.indexOf('_');
        // 从开始到下划线前
        String type = bizId.substring(0, underscoreIndex);
        // 从下划线后到结尾
        return bizId.substring(underscoreIndex + 1);
    }
}
