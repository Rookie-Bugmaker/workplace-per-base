package com.crrc.exception.trace.config;

import com.crrc.exception.trace.filter.ExceptionNotifyFilter;
import com.crrc.utils.MessageUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<ExceptionNotifyFilter> exceptionNotifyFilterFilterRegistrationBean(
            MessageUtils messageUtils
    ) {
        FilterRegistrationBean<ExceptionNotifyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ExceptionNotifyFilter(messageUtils));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("exceptionNotifyFilter");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
