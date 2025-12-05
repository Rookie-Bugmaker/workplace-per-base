package com.crrc.utils;

import java.time.DateTimeException;
import java.time.LocalDate;


public class DateValidator {
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    /**
     * 严格校验日期合法性
     * @param date 待校验日期
     * @return true-合法日期, false-非法日期
     */
    public static boolean isValidDate(LocalDate date) {
        // null也为合法日期
        if (date == null) {
            return true;
        }

        // 年份范围检查
        int year = date.getYear();
        if (year < MIN_YEAR || year > MAX_YEAR) {
            return false;
        }

        // 通过重建日期对象验证
        try {
            LocalDate verified = LocalDate.of(year, date.getMonthValue(), date.getDayOfMonth());
            return verified.equals(date);
        } catch (DateTimeException e) {
            return false;
        }
    }
}
