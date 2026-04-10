package com.crrc.exception.trace.utils;

import java.util.*;

public final class ExceptionTraceUtils {

    private static final StackTraceElement UNKNOWN =
            new StackTraceElement("UnknownClass", "unknownMethod", "UnknownSource", -1);

    private ExceptionTraceUtils() {
    }

    /**
     * 从异常链中找到“最合适”的错误位置：
     * 1. 优先从 root cause 开始查找
     * 2. 优先选择属于自己项目包名的栈帧
     * 3. 如果找不到，则退回到第一条栈帧
     */
    public static StackTraceElement findBestFrame(Throwable ex, Collection<String> projectBasePackages) {
        Objects.requireNonNull(ex, "ex must not be null");

        List<Throwable> chain = buildCauseChain(ex);
        Collections.reverse(chain); // root cause 优先

        // 第一轮：找项目内代码
        for (Throwable throwable : chain) {
            StackTraceElement matched = findFirstProjectFrame(throwable.getStackTrace(), projectBasePackages);
            if (matched != null) {
                return matched;
            }
        }

        // 第二轮：实在找不到项目内代码，就取第一条栈帧
        for (Throwable throwable : chain) {
            StackTraceElement[] stack = throwable.getStackTrace();
            if (stack != null && stack.length > 0) {
                return stack[0];
            }
        }

        return UNKNOWN;
    }

    /**
     * 获取最底层 root cause。
     */
    public static Throwable getRootCause(Throwable ex) {
        Objects.requireNonNull(ex, "ex must not be null");

        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = ex;
        Throwable root = ex;

        while (current != null && seen.add(current)) {
            root = current;
            current = current.getCause();
        }
        return root;
    }

    /**
     * 将栈帧格式化成便于日志/告警阅读的文本。
     */
    public static String formatFrame(StackTraceElement frame) {
        if (frame == null) {
            return "UnknownSource";
        }

        String fileName = frame.getFileName() != null ? frame.getFileName() : "UnknownSource";
        String lineNumber = frame.getLineNumber() >= 0 ? String.valueOf(frame.getLineNumber()) : "UnknownLine";

        return frame.getClassName() + "." + frame.getMethodName()
                + "(" + fileName + ":" + lineNumber + ")";
    }

    private static StackTraceElement findFirstProjectFrame(StackTraceElement[] stack,
                                                           Collection<String> projectBasePackages) {
        if (stack == null || stack.length == 0) {
            return null;
        }

        if (projectBasePackages == null || projectBasePackages.isEmpty()) {
            return stack[0];
        }

        for (StackTraceElement element : stack) {
            if (isProjectClass(element.getClassName(), projectBasePackages)) {
                return element;
            }
        }
        return null;
    }

    private static boolean isProjectClass(String className, Collection<String> projectBasePackages) {
        if (className == null || projectBasePackages == null) {
            return false;
        }

        for (String basePackage : projectBasePackages) {
            if (basePackage == null || basePackage.isEmpty()) {
                continue;
            }
            if (className.equals(basePackage) || className.startsWith(basePackage + ".")) {
                return true;
            }
        }
        return false;
    }

    private static List<Throwable> buildCauseChain(Throwable ex) {
        List<Throwable> chain = new ArrayList<>();
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        Throwable current = ex;
        while (current != null && seen.add(current)) {
            chain.add(current);
            current = current.getCause();
        }
        return chain;
    }
}