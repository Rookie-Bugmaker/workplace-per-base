package com.crrc.utils;

public class CodeValueComparatorUtils {

    /**
     * 比较人员分类7的大小，空值视为最小，当前只支持001-005的比较，新加码值这里需要处理
     * @param t1 1
     * @param t2 2
     * @return 1:t1 > t2, 0: t1 = t2, -1: t1 < t2
     */
    public static int compareCategory7(String t1, String t2) {
        if(t1 == null && t2 != null) {
            return 1;
        }

        if(t1 != null && t2 == null) {
            return -1;
        }

        if(t1 == null) {
            return 0;
        }

        return Integer.compare(Integer.parseInt(t1), Integer.parseInt(t2));
    }
}
