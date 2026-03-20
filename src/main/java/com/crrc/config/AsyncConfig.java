package com.crrc.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean("templateGenerateAsyncExecutor")
    public ThreadPoolTaskExecutor templateGenerateAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：常驻线程
        executor.setCorePoolSize(8);

        // 最大线程数：队列满后可扩容到的上限
        executor.setMaxPoolSize(16);

        // 队列容量：一定要设为有界，避免无限堆积
        executor.setQueueCapacity(200);

        // 非核心线程空闲存活时间（秒）
        executor.setKeepAliveSeconds(60);

        // 线程名前缀：便于日志和监控定位
        executor.setThreadNamePrefix("template-generate-async-");

        // 关闭容器时等待任务执行完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 允许核心线程超时回收（可选）
        executor.setAllowCoreThreadTimeOut(true);

        // 拒绝策略：让提交任务的调用线程自己执行，起到反压作用
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }

    /**
     * 指定 @Async 默认使用的执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return templateGenerateAsyncExecutor();
    }

    /**
     * 处理 void 返回值异步方法的异常
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(@NotNull Throwable ex, @NotNull Method method, @NotNull Object... params) {
                log.error("异步方法执行异常. method={}, params={}", method.getName(), params, ex);
            }
        };
    }
}
