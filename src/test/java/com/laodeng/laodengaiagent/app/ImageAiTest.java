package com.laodeng.laodengaiagent.app;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/26 18:46
 * @description
 */

@Log4j2
@SpringBootTest
public class ImageAiTest {
    @Autowired
    private ImageApp imageApp;

    /**
     * 图片识别测试
     */
    @Test
    void imageAnalysisByEntity() {
        String chatId = UUID.randomUUID().toString();
        System.out.println(imageApp.promptChat("分析图中文字,中文回答", chatId, "picture/img.png"));
    }

    @Test
    void imageAnalysisByText() {
        String chatId = UUID.randomUUID().toString();
        System.out.println(imageApp.imageChat("分析图中文字,中文回答", chatId, "picture/img.png"));
    }

    


}
