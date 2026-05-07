package com.crrc.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class YearMonthStringConverter implements Converter<YearMonth> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public Class<?> supportJavaTypeKey() {
        return YearMonth.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<YearMonth> context) {
        YearMonth value = context.getValue();
        return new WriteCellData<>(value == null ? "" : value.format(FORMATTER));
    }
}