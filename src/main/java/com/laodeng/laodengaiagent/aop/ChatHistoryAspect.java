package com.laodeng.laodengaiagent.aop;

import com.laodeng.laodengaiagent.charmemory.RedisBaseChatMemory;
import com.laodeng.laodengaiagent.domain.po.StreamEvent;
import com.laodeng.laodengaiagent.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/3 21:17
 * @description
 */

@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class ChatHistoryAspect {

    private final ChatHistoryService chatHistoryService;
    private final RedisBaseChatMemory redisBaseChatMemory;

    @Around("execution(* com.laodeng.laodengaiagent.app.ReactAgentApp.execute(..))")
    public Object saveHistoryFromReactAgentExecute(ProceedingJoinPoint point) throws Throwable {
        log.info("开始截取当前对话记录保存到长期记忆中");
        //直接使用反射获取当前环绕增强的方法的两个参数值
        Object[] args = point.getArgs();
        String input = (String) args[0]; //用户输入内容
        String memoryId = (String) args[1]; // 会话记忆ID
        //保存用户输入信息以及记忆存储ID也就是会话ID
        chatHistoryService.saveMessage(memoryId, "USER", input, "REACT");
        //执行当前原方法,并将运行结果作为返回值返回
        Object result = point.proceed();

        if (result instanceof Flux<?> flux){
            StringBuilder fullText = new StringBuilder();
            AtomicBoolean hasError = new AtomicBoolean(true);
            return flux.doOnNext(event -> {
                if (event instanceof StreamEvent se){
                    if ("text".equals(se.getType())) {
                        fullText.append(se.getData());
                    } else if ("error".equals(se.getType())) {
                        hasError.set(false);
                    }
                }
            }).doOnComplete(() -> {
                if (!hasError.get()){
                    log.info("执行失败");
                    chatHistoryService.deleteLastMessage(memoryId, "USER");
                    redisBaseChatMemory.removeLastN(memoryId, 1);
                }else {
                    log.info("保存对话记录到长期记忆中");
                    chatHistoryService.saveMessage(memoryId, "ASSISTANT", fullText.toString(), "REACT");
                }
            });
        }
        return result;
    }


}
