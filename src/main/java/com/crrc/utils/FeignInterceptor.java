package com.crrc.utils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (!ObjectUtils.isEmpty(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            requestTemplate.header(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
            //项目编码
            String x_PROJECT_CODE = "X-Project-Code";
            requestTemplate.header(x_PROJECT_CODE, request.getHeader(x_PROJECT_CODE));
        }
    }
}