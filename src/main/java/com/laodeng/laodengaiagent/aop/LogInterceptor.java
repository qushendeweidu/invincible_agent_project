package com.laodeng.laodengaiagent.aop;

import java.util.UUID;

import com.laodeng.laodengaiagent.service.InfluxDBService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求响应日志 AOP
 **/
@Aspect
@Component
@Log4j2
@RequiredArgsConstructor
public class LogInterceptor {
    private final InfluxDBService influxDBService;

    /**
     * 执行拦截
     */
    @Around("execution(* com.laodeng.laodengaiagent.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString();
        String url = httpServletRequest.getRequestURI();
        // 获取请求参数
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        // 输出请求日志
        log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url,
                httpServletRequest.getRemoteHost(), reqParam);
        int statusCode = 200;
        try {
            // 执行原方法
            Object result = point.proceed();
            // 输出响应日志
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
            influxDBService.recordApiRequest(url, httpServletRequest.getMethod(), totalTimeMillis, statusCode);
            return result;
        } catch (Exception e) {
            // 将异常日志记录到influxDB中
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            log.error("request error, id: {}, cost: {}ms", requestId, totalTimeMillis, e);
            influxDBService.recordApiRequest(url, httpServletRequest.getMethod(), totalTimeMillis, statusCode);
            throw e;
        }
    }
}

