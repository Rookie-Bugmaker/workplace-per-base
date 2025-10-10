package com.crrc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.crrc.mapper")
public class PerCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(PerCoreApplication.class, args);
    }
}