package com.laodeng.laodengaiagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.laodeng.laodengaiagent.app.ReactAgentApp;
import com.laodeng.laodengaiagent.domain.po.StreamEvent;
import com.laodeng.laodengaiagent.service.AIAppService;
import com.laodeng.laodengaiagent.service.MinioService;
import com.laodeng.laodengaiagent.service.UserService;
import com.laodeng.laodengaiagent.utils.TTSUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.Executor;


/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/23 13:56
 * @description
 */

@Log4j2
@Service
@Transactional(rollbackOn = Exception.class)
public class ReactAgentServiceImpl implements AIAppService {
    private final ReactAgentApp reactAgentApp;
    private final MinioService minioService;
    private final TTSUtils ttsUtils;
    private final UserService userService;
    private final Executor ttsExecutor;

    public ReactAgentServiceImpl(
            ReactAgentApp reactAgentApp,
            MinioService minioService,
            TTSUtils ttsUtils,
            UserService userService,
            @Qualifier("ttsExecutor") Executor ttsExecutor) {
        this.reactAgentApp = reactAgentApp;
        this.minioService = minioService;
        this.ttsUtils = ttsUtils;
        this.userService = userService;
        this.ttsExecutor = ttsExecutor;
    }

    @Override
    public AppType getAppType() {
        return AppType.REACT;
    }

    /**
     * 多智能体对话执行
     * @param input 用户提问信息
     * @return String 经过多智能体协作处理后的AI回答
     */
    public Flux<StreamEvent> execute(String input, Boolean tts, String ttsSuffix) throws GraphRunnerException,IOException {
        Long loginId = StpUtil.getLoginIdAsLong();
        String memoryId = userService.getById(loginId).getUsername();
        Flux<StreamEvent> streamEventFlux = reactAgentApp.execute(input, memoryId)
                .distinctUntilChanged(e ->
                        "thinking".equals(e.getType())
                                ? "thinking"
                                : e.getType() + ":" + e.getTimestamp()
                )
                .cache();
        // 2. 不需要 TTS：直接返回共享流 + done
        if (!Boolean.TRUE.equals(tts)) {
            return Flux.concat(streamEventFlux, Flux.just(StreamEvent.of("done", "模型答复完毕")));
        }
        // 从流式对象中采用响应式的方式获取需要加工的数据
        Mono<StreamEvent> ttsMono = streamEventFlux
                .filter(e -> "text".equals(e.getType()))
                .map(StreamEvent::getData)
                .reduce("", String::concat)
                .flatMap(
                        output ->{
                            String ttsFilePath = ttsUtils.streamAudioDataToSpeaker(ttsSuffix, output);
                            String ttsUrl = minioService.getPresignedUrl("tts",ttsFilePath,60*60*6);
                            output = ttsUrl + "  智能体回复: \n" +output;
                            return Mono.just(StreamEvent.of("text",output));
                        }
                )
                .subscribeOn(Schedulers.fromExecutor(ttsExecutor))
                .onErrorResume(throwable -> Mono.just(StreamEvent.of("error",throwable.getMessage())));
        return Flux.concat(streamEventFlux,ttsMono);

    }

    public String executeOutPutByString(String input, Boolean tts, String ttsSuffix) throws GraphRunnerException, IOException {
        String output = reactAgentApp.execute(input, userService.getById(StpUtil.getLoginIdAsLong()).getUsername())
                .filter(se -> "text".equals(se.getType()))
                .map(StreamEvent::getData)
                .reduce("", String::concat)   // 逐个拼接
                .block();                           // 阻塞等结果
        if (tts && output != null && !output.isEmpty()){
            String ttsFilePath = ttsUtils.streamAudioDataToSpeaker(ttsSuffix, output);
            String ttsUrl = minioService.getPresignedUrl("tts",ttsFilePath,60*60*6);
            output = ttsUrl + "\n 智能体回复: \n" +output;
        }
        return output;
    }

}
