package com.crrc.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author yukun
 */
public final class ConstantsUtils {

    private ConstantsUtils() {}

    // 用于Constants转Map，将filed和filedDesc拼装成Map
    // 在AgreeAnnualServiceImpl中使用，将审批状态从code转换到描述
    public static Map<String, String> valueToDescMap(Class<?> constantsClass) {
        Map<String, String> nameToValue = new HashMap<>();
        Map<String, String> baseToDesc  = new HashMap<>();

        for (Field f : constantsClass.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && f.getType() == String.class) {
                try {
                    String fieldName = f.getName();
                    String fieldVal  = (String) f.get(null);
                    String upperName = fieldName.toUpperCase(Locale.ROOT);
                    // 匹配以_DESCRIPTION结尾的字段名
                    if (upperName.endsWith("_DESCRIPTION") || upperName.endsWith("_DESC")) {
                        String base = upperName.replaceFirst("(_DESCRIPTION|_DESC)$", "");
                        baseToDesc.put(base, fieldVal);
                    } else {
                        nameToValue.put(upperName, fieldVal);
                    }
                } catch (IllegalAccessException ignored) {
                    /* 任意一步报错，正常不会，忽略 */
                }
            }
        }

        // 生成 value -> desc
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : nameToValue.entrySet()) {
            String base = e.getKey();
            String desc = baseToDesc.get(base);
            if (desc != null) {
                result.put(e.getValue(), desc);
            }
        }
        return result;
    }
}
