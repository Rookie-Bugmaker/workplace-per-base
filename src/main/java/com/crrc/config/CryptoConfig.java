package com.crrc.config;

import com.crrc.utils.AesGcmB64B64CryptoUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class CryptoConfig {
    @Bean
    public SecretKey secretKey() {
        return AesGcmB64B64CryptoUtils.keyFromBase64("TVlTUUxCWVBBU1NTRUNSRVRLRVlGT1JJTkpFQ1QxMjM=");
    }
}
