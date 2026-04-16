package com.crrc.mongodb.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ReadingConverter
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        return LocalDate.parse(source, FORMATTER);
    }
}