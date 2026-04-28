package com.laodeng.laodengaiagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.laodeng.laodengaiagent.app.LoveApp;
import com.laodeng.laodengaiagent.service.AIAppService;
import com.laodeng.laodengaiagent.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static cn.dev33.satoken.SaManager.log;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/12 11:21
 * @description
 */

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class LoveAppServiceImpl implements AIAppService {
    private final LoveApp loveApp;
    private final UserService userService;

    @Override
    public AppType getAppType() {
        return AppType.LOVE;
    }

    /**
     * RAG增强对话
     * @param userMessageInsert 用户提问信息
     * @return String 基于知识库检索增强的AI回答
     */
    public String ragChat(String userMessageInsert) {

        String result = loveApp.doChatWithRag(userMessageInsert, userService.getById(StpUtil.getLoginIdAsLong()).getUsername());
        log.info("最终RAG检索结果: {}", result);
        return result;
    }


    /**
     * 流式输出对话
     * @param userMessageInsert 用户提问信息
     * @return Flux<String> 流式输出的AI回答
     */
    public Flux<String> doChatWithStream(String userMessageInsert) {
        return loveApp.doChatWithStream(userMessageInsert, userService.getById(StpUtil.getLoginIdAsLong()).getUsername());
    }
}
