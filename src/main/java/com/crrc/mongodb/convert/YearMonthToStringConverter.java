package com.crrc.mongodb.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import java.time.YearMonth;

@WritingConverter
public class YearMonthToStringConverter implements Converter<YearMonth, String> {

    @Override
    public String convert(YearMonth source) {
        // YearMonth 默认的 toString() 就是 yyyy-MM 格式
        return source.toString();
    }
}