package com.laodeng.laodengaiagent.charmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/22 16:43
 * @description 文件存储的会话记忆
 */
@Builder
@Log4j2
public class FileBaseChatMemory implements ChatMemory {

    private final String BASE_PATH;
    // 使用 ThreadLocal 解决线程安全问题
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 关键配置 1: 禁用注册表（你已经做了）
        kryo.setRegistrationRequired(false);
        // 关键配置 2: 支持循环引用（Message 对象可能存在这种结构）
        kryo.setReferences(true);
        // 关键配置 3: 实例化策略（你已经做了）
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        // 关键配置 4: 注册常用的集合类，防止 ID 冲突
        kryo.register(ArrayList.class);
        return kryo;
    });

    public FileBaseChatMemory(String basePath) {
        // Paths.get 会自动处理不同系统的斜杠 (Windows 用 \, Linux 用 /)
        this.BASE_PATH = Paths.get(System.getProperty("user.dir"), basePath)
                .toAbsolutePath()
                .toString();

        File file = new File(this.BASE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    @Override
    public void add(String conversationId, Message message) {
        // 1. 先把以前的取出来
        List<Message> messages = new ArrayList<>(get(conversationId));
        // 2. 把新的加进去
        messages.add(message);
        // 3. 全量保存回文件
        saveConversation(conversationId, messages);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> existingMessages = new ArrayList<>(get(conversationId));
        existingMessages.addAll(messages);
        saveConversation(conversationId, existingMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messages = getOrCreateConversation(conversationId);
        log.info("messages: {}", messages);
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        File file = new File(BASE_PATH + conversationId + ".kryo");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 每个绘画文件单独保存
     * @param conversationId 会话ID
     * @return 文件
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_PATH, conversationId + ".kryo");
    }

    /**
     * 获取或者创建会话消息
     * @param conversationId 会话ID
     * @return 会话消息
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists() && file.length() > 0) {
            try (Input input = new Input(new FileInputStream(file))) {
                // 获取当前线程的 Kryo 实例
                return kryoThreadLocal.get().readObject(input, ArrayList.class);
            } catch (Exception e) {
                log.error("反序列化失败，可能是文件损坏或版本不兼容: {}", file.getAbsolutePath());
                // 如果读取失败，建议删除损坏的文件，防止循环报错
                // file.delete();
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 保存会话消息
     * @param conversationId 会话ID
     * @param newMessages 新消息
     */
    private void saveConversation(String conversationId, List<Message> newMessages) {
        // 注意：add 方法通常是追加，而你的逻辑可能是覆盖。
        // Spring AI 的 ChatMemory 接口通常期望 add 是追加到内存，此处建议先读再写，或者确保外部传入的是全量 List
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryoThreadLocal.get().writeObject(output, new ArrayList<>(newMessages));
        } catch (java.io.IOException e) {
            log.error("保存会话失败", e);
        }
    }


}
