package com.crrc.exception.trace.filter;

import com.crrc.exception.trace.utils.ExceptionTraceUtils;
import com.crrc.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ExceptionNotifyFilter extends OncePerRequestFilter {

    private final MessageUtils messageUtils;
    private final String env;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            notifySafely(request, ex);
            throw ex;
        }
    }

    /**
     * 可按需忽略某些路径，例如健康检查、swagger、静态资源等
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/static");
    }

    private void notifySafely(HttpServletRequest request, Exception ex) {
        List<String> projectPackages = Collections.singletonList("com.crrc"); // 改成你自己的项目根包

        StackTraceElement bestFrame = ExceptionTraceUtils.findBestFrame(ex, projectPackages);
        Throwable rootCause = ExceptionTraceUtils.getRootCause(ex);

        String content = String.format(
                "%s：请求方法: %s%n请求路径: %s%n查询参数: %s%n客户端IP: %s%n异常类型: %s%n异常消息: %s%n错误位置: %s%n错误文件: %s%n错误行号: %s%ntraceId: %s",
                "test".equals(env) ? "测试环境" : "生产环境",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                getClientIp(request),
                rootCause.getClass().getName(),
                rootCause.getMessage(),
                ExceptionTraceUtils.formatFrame(bestFrame),
                bestFrame.getFileName() != null ? bestFrame.getFileName() : "UnknownSource",
                bestFrame.getLineNumber() >= 0 ? bestFrame.getLineNumber() : "UnknownLine",
                TraceContext.traceId()
        );

        try {
            messageUtils.pushPlainMsg(Arrays.asList("013200031192", "013200031524"), content);
        } catch (Exception notifyEx) {
            // 通知失败不能影响原始异常继续抛出
            log.error("异常通知发送失败", notifyEx);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
