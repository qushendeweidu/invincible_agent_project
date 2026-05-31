package com.laodeng.laodengaiagent.controller.chat;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.laodeng.laodengaiagent.app.ImageApp;
import com.laodeng.laodengaiagent.app.ReactAgentApp;
import com.laodeng.laodengaiagent.common.R;
import com.laodeng.laodengaiagent.domain.dto.AiModelConfigDTO;
import com.laodeng.laodengaiagent.domain.po.ChatHistoryMessage;
import com.laodeng.laodengaiagent.domain.po.StreamEvent;
import com.laodeng.laodengaiagent.domain.vo.AiModelConfigVO;
import com.laodeng.laodengaiagent.service.AiModelConfigService;
import com.laodeng.laodengaiagent.service.ChatHistoryService;
import com.laodeng.laodengaiagent.service.factory.AIAppServiceFactory;
import com.laodeng.laodengaiagent.service.impl.ImageAppServiceImpl;
import com.laodeng.laodengaiagent.service.impl.LoveAppServiceImpl;
import com.laodeng.laodengaiagent.service.impl.ReactAgentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/20 13:22
 * @description 聊天调用控制器
 */

@Log4j2
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final AIAppServiceFactory aiAppServiceFactory;
    private final AiModelConfigService aiModelConfigService;
    private final ChatHistoryService chatHistoryService;

    /**
     * 流式输出对话
     * @param msg 用户提问信息
     * @return Flux<String> 也就是流式输出
     */
    @SaCheckPermission(value = "user.ai_base")
    @GetMapping("/chat_stream")
    public Flux<String> doChatWithStream(String msg) {
        LoveAppServiceImpl service = (LoveAppServiceImpl) aiAppServiceFactory.getLoveAppService();
        return (service.doChatWithStream(msg));
    }

    /**
     * RAG增强对话
     * @param msg 用户提问信息
     * @return String 基于知识库检索增强的AI回答
     */
    @SaCheckPermission(value = "user.ai_max")
    @GetMapping("/chat")
    public R<String> doChatWhitRAG(String msg) {
        LoveAppServiceImpl service = (LoveAppServiceImpl) aiAppServiceFactory.getLoveAppService();
        return R.ok(service.ragChat(msg));
    }

    /**
     * 多智能体对话流式输出对话
     * @param msg 用户提问信息
     * @return String 经过多智能体协作处理后的AI回答
     */
    @SaCheckPermission(value = "user.ai_max")
    @GetMapping(value = "/react_agent_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StreamEvent> doChatWithReactAgent(
            String msg,
            @RequestParam(defaultValue = "") String ttsSuffix
            ) throws GraphRunnerException, IOException {
        ReactAgentServiceImpl service = (ReactAgentServiceImpl) aiAppServiceFactory.getReactAppService();
        return service.execute(msg,true,ttsSuffix);
    }


    /**
     * 图片分析
     * @param msg 用户提问信息
     * @param picturePath 图片路径（MinIO对象名称或URL)
     * @return ContentUnderstand 图片内容理解结果
     */
    @SaCheckPermission(value = "user.ai_base")
    @GetMapping("/image_analyse")
    public R<ImageApp.ContentUnderstand> pictureAnalyse(String msg,String picturePath) {
        ImageAppServiceImpl service = (ImageAppServiceImpl) aiAppServiceFactory.getImageAppService();
        return R.ok(service.contextLoads(msg,picturePath));
    }

    /**
     * 添加AI模型配置
     * @param aiModelConfigDTO 模型配置信息
     * @return R<Boolean> 添加结果
     */
    @SaCheckRole(value = "admin")
    @PostMapping("/add_model_config")
    public R<Boolean> addModelConfig(AiModelConfigDTO aiModelConfigDTO) {
        boolean save = aiModelConfigService.addModelConfig(aiModelConfigDTO);
        log.info("模型保存结果:{}", save);
        return R.ok(save);
    }

    /**
     * 更新AI模型配置
     * @param aiModelConfigDTO 模型配置信息
     * @return R<Boolean> 更新结果
     */
    @SaCheckRole(value = "admin")
    @PatchMapping("/update_model_config")
    public R<Boolean> updateModelConfig(AiModelConfigDTO aiModelConfigDTO) {
        boolean update = aiModelConfigService.updateModelConfig(aiModelConfigDTO);
        log.info("update:{}", update);
        return R.ok(update);
    }

    /**
     * 删除AI模型配置
     * @param id 模型配置ID
     * @return R<Boolean> 删除结果
     */
    @SaCheckRole(value = "admin")
    @DeleteMapping("/remove_model_config/{id}")
    public R<Boolean> removeModelConfig(@PathVariable Long id) {
        boolean remove = aiModelConfigService.removeByIdWithEvent(id);
        log.info("remove:{}", remove);
        return R.ok(remove);
    }

    /**
     * 获取所有AI模型配置列表
     * @return R<List<AiModelConfigVO>> 模型配置列表
     */
    @SaCheckRole(value = "admin")
    @GetMapping("/list_model_config")
    public R<List<AiModelConfigVO>> listModelConfig() {
        List<AiModelConfigVO> aiModelConfigs = aiModelConfigService.getAllConfig();
        return R.ok(aiModelConfigs);
    }

    /**
     * 根据ID获取AI模型配置
     * @param id 模型配置ID
     * @return R<AiModelConfigVO> 模型配置信息
     */
    @SaCheckRole(value = "admin")
    @GetMapping("/get_model_id/{id}")
    public R<AiModelConfigVO> getModelConfig(@PathVariable Long id) {
        AiModelConfigVO aiModelConfig = aiModelConfigService.getConfigsById(id);
        log.info("aiModelConfigVO:{}", aiModelConfig);
        return R.ok(aiModelConfig);
    }

    /**
     * 获取所有的模型key
     * @return R<Set<String>> 模型key列表
     */
    public R<Set<String>> getAllModelKeys(){
        return R.ok(aiModelConfigService.getAllModelKeys());
    }

    /**
     * 获取AI模型对话历史
     * @param memoryId 会话记忆ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return  R<Page<ChatHistoryMessage>> 模型对话历史
     */
    @SaCheckRole(value = "user.ai_base")
    @GetMapping("/get_ai_history")
    public R<Page<ChatHistoryMessage>> getAIHistory(String memoryId, Integer pageNum, Integer pageSize) {
        Page<ChatHistoryMessage> page = chatHistoryService.getHistory(memoryId, pageNum, pageSize);
        return R.ok(page);
    }

    /**
     * 删除过往对话记忆
     * @param messageId 会话消息ID
     * @return R<Boolean> 删除结果
     */
    @SaCheckRole(value = "user.ai_base")
    @DeleteMapping("/withdraw_message/{messageId}")
    public R<Boolean> withdrawMessage(@PathVariable String messageId) {
        boolean withdraw = chatHistoryService.withdrawMessage(messageId);
        log.info("withdraw:{}", withdraw);
        return R.ok(withdraw);
    }


}
