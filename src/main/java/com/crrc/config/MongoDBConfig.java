package com.crrc.config;

import com.crrc.mongodb.convert.StringToLocalDateConverter;
import com.crrc.mongodb.convert.StringToYearMonthConverter;
import com.crrc.mongodb.convert.YearMonthToStringConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoDBConfig {
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new YearMonthToStringConverter(),
                new StringToYearMonthConverter(),
                new StringToLocalDateConverter()
        ));
    }
}
