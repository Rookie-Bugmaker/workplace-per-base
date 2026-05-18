package com.crrc.exception.trace.notifier;

import com.crrc.entity.dict.vo.DictItemVO;
import com.crrc.entity.dict.vo.DictVO;
import com.crrc.exception.trace.utils.ExceptionTraceUtils;
import com.crrc.utils.DictUtils;
import com.crrc.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExceptionNotifier {

    private static final List<String> PROJECT_PACKAGES = Collections.singletonList("com.crrc");
    private final MessageUtils messageUtils;
    private final String env;
    private final DictUtils dictUtils;

    public ExceptionNotifier(MessageUtils messageUtils,
                             @Value("${crrc.env:prod}") String env,
                             DictUtils dictUtils
    ) {
        this.messageUtils = messageUtils;
        this.env = env;
        this.dictUtils = dictUtils;
    }

    public void notifySafely(HttpServletRequest request, Throwable ex) {
        try {
            String method = request != null ? request.getMethod() : "N/A";
            String uri = request != null ? request.getRequestURI() : "N/A";
            String query = request != null ? request.getQueryString() : "N/A";
            String clientIp = request != null ? getClientIp(request) : "N/A";

            doNotify(method, uri, query, clientIp, ex);
        } catch (Exception notifyEx) {
            log.error("notifySafely(HttpServletRequest request, Throwable ex): 异常通知发送失败", notifyEx);
        }
    }

    public void notifySafely(Throwable ex) {
        try {
            doNotify("", "", "", "", ex);
        } catch (Exception notifyEx) {
            log.error("notifySafely(Throwable ex): 异常通知发送失败", notifyEx);
        }
    }

    public void notifySafely(String methodName, String uri, String query, String clientIp, Throwable ex) {
        try {
            doNotify(methodName, uri, query, clientIp, ex);
        } catch (Exception notifyEx) {
            log.error("notifySafely(String methodName, String uri, String query, String clientIp, Throwable ex): 异常通知发送失败", notifyEx);
        }
    }

    private void doNotify(String method, String uri, String query, String clientIp, Throwable ex) {
        // 获得通知人
        DictVO hrscExceptionNotifier = dictUtils.get("HRSC_EXCEPTION_NOTIFIER");
        if(hrscExceptionNotifier == null || hrscExceptionNotifier.getItems() == null || hrscExceptionNotifier.getItems().isEmpty()) {
            return;
        }

        List<String> receives = hrscExceptionNotifier.getItems().stream().map(DictItemVO::getItemCode).collect(Collectors.toList());


        // 先将错误打印到日志
        StackTraceElement bestFrame = ExceptionTraceUtils.findBestFrame(ex, PROJECT_PACKAGES);
        Throwable rootCause = ExceptionTraceUtils.getRootCause(ex);

        String content = String.format(
                "per-require %s 出现异常:%n请求方法: %s%n请求路径: %s%n查询参数: %s%n客户端IP: %s%n异常类型: %s%n异常消息: %s%n错误位置: %s%n错误文件: %s%n错误行号: %s%ntraceId: %s",
                "test".equals(env) ? "测试环境" : "生产环境",
                method,
                uri,
                query,
                clientIp,
                rootCause.getClass().getName(),
                rootCause.getMessage(),
                ExceptionTraceUtils.formatFrame(bestFrame),
                bestFrame != null && bestFrame.getFileName() != null ? bestFrame.getFileName() : "UnknownSource",
                bestFrame != null && bestFrame.getLineNumber() >= 0 ? bestFrame.getLineNumber() : "UnknownLine",
                TraceContext.traceId()
        );

        log.error(content, ex);

        messageUtils.pushPlainMsg(receives, content);
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