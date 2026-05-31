package com.laodeng.laodengaiagent.advisor;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.Objects;


/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/20 18:36
 * @description 日志拦截
 */
@Log4j2
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 请求拦截
     * @param chatClientRequest 请求
     * @param callAdvisorChain 链
     * @return 响应
     */
    @Override
    public ChatClientResponse adviseCall(@NotNull ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    protected void logRequest(ChatClientRequest request) {
        log.debug("AI请求: {}", request.prompt().getUserMessage().toString());
    }

    protected void logResponse(ChatClientResponse response) {
        log.debug("AI响应: {}", Objects.requireNonNull(response.chatResponse()).getResult().getOutput().getText());
    }

    @NotNull
    @Override
    public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return 0;
    }
}
