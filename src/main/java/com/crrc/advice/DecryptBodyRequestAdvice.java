package com.crrc.advice;

import com.crrc.annotation.DecryptBody;
import com.crrc.utils.AesGcmB64B64CryptoUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 解密流
 */
@ControllerAdvice
public class DecryptBodyRequestAdvice extends RequestBodyAdviceAdapter {
    private final SecretKey aesKey;

    public DecryptBodyRequestAdvice(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(DecryptBody.class)
                || (methodParameter.getMethod() != null && methodParameter.getMethod().isAnnotationPresent(DecryptBody.class));
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        DecryptBody decryptBody = parameter.getParameterAnnotation(DecryptBody.class);
        if(decryptBody == null && parameter.getMethod() != null) {
            decryptBody = parameter.getMethod().getAnnotation(DecryptBody.class);
        }

        if(decryptBody == null) {
            return inputMessage;
        }

        byte[] raw = StreamUtils.copyToByteArray(inputMessage.getBody());

        if(raw.length == 0) {
            return inputMessage;
        }

        String outerB64 = new String(raw, StandardCharsets.UTF_8).trim();

        if(outerB64.length() >= 2 && outerB64.startsWith("\"") && outerB64.endsWith("\"")) {
            outerB64 = outerB64.substring(1, outerB64.length() - 1);
        }

        byte[] aad = resolveAadBytes(inputMessage, decryptBody);

        String json = AesGcmB64B64CryptoUtils.decryptOuterBase64ToJson(outerB64, aesKey, aad);

        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();

        headers.putAll(inputMessage.getHeaders());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLength(jsonBytes.length);

        return new SimpleHttpInputMessage(headers, new ByteArrayInputStream(jsonBytes));
    }

    private byte[] resolveAadBytes(HttpInputMessage inputMessage, DecryptBody decryptBody) {
        if (decryptBody.aadMode() == DecryptBody.AadMode.NONE) {
            return null;
        }

        if (decryptBody.aadMode() == DecryptBody.AadMode.URI) {
            if (inputMessage instanceof ServletServerHttpRequest) {
                String uri = ((ServletServerHttpRequest) inputMessage).getServletRequest().getRequestURI();
                return uri.getBytes(StandardCharsets.UTF_8);
            }
            return null;
        }

        // HEADER
        String headerVal = inputMessage.getHeaders().getFirst(decryptBody.aadHeader());
        if (headerVal == null || headerVal.trim().isEmpty()) return null;
        return headerVal.getBytes(StandardCharsets.UTF_8);
    }

    private static class SimpleHttpInputMessage implements HttpInputMessage {

        private final HttpHeaders headers;
        private final InputStream body;

        private SimpleHttpInputMessage(HttpHeaders headers, InputStream body) {
            this.headers = headers;
            this.body = body;
        }

        @Override public HttpHeaders getHeaders() { return headers; }
        @Override public InputStream getBody() { return body; }
    }
}
