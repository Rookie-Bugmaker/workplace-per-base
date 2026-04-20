package com.crrc.mongodb.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@ReadingConverter
public class StringToYearMonthConverter implements Converter<String, YearMonth> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public YearMonth convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        return YearMonth.parse(source, FORMATTER);
    }
}