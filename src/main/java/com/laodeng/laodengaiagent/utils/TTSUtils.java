package com.laodeng.laodengaiagent.utils;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.laodeng.laodengaiagent.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/12 20:56
 * @description
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class TTSUtils {
    private final MinioService minioService;
    @Value("${ALIYUN_TTS_KEY}")
    private String ttsKey;
    // 模型
    private static String model = "cosyvoice-v3-flash";
    // 音色
    private static String voice = "longwan_v3";
    // SSML 参数（基于真人对话语速研究调整：日常闲聊比TTS默认快约20%）
    private static final String DEFAULT_RATE = "1.1";
    private static final String DEFAULT_PITCH = "1.0";
    private static final String DEFAULT_VOLUME = "75";

    // 匹配完整括号动作描写：中文括号（…）和英文括号(...)
    private static final Pattern ACTION_DESC_PATTERN = Pattern.compile("[（\\(][^）\\)]+[）\\)]");
    // 兜底：匹配不闭合的左括号到行尾（如 "（傻瓜，我当然想啊……"）
    private static final Pattern UNCLOSED_BRACKET_PATTERN = Pattern.compile("[（\\(][^）\\)]+$");
    // 清理残留空括号
    private static final Pattern EMPTY_BRACKET_PATTERN = Pattern.compile("[（\\(][）\\)]");

    private static Boolean isSSML = true;

    public String streamAudioDataToSpeaker(String suffix, String agentCallBack){
        // 想远程的阿里云服务器发起请求的参数
        SpeechSynthesisParam param =
                SpeechSynthesisParam.builder()
                        .apiKey(ttsKey)
                        .model(model) // 模型
                        .voice(voice) // 音色
                        .build();

        // 同步模式：禁用回调（第二个参数为null）
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
        ByteBuffer audio = null;
        try {
            // 使用SSML包装文本，控制语速、语调、音量
            String ssmlText;
            String cleaned = stripActionDesc(agentCallBack);
            if (isSSML) {
                ssmlText = wrapWithSSML(cleaned);
            } else {
                ssmlText = cleaned;
            }
            log.info("SSML文本：{}", ssmlText);
            audio = synthesizer.call(ssmlText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 任务结束关闭websocket连接
            synthesizer.getDuplexApi().close(1000, "bye");
        }
        if (audio != null) {
            // 生成即将存入minio的音频文件名后续用于地址拼接
            String objectName = "tts_" + System.currentTimeMillis() + suffix;
            byte[] audioData = audio.array();
            // 尝试将音频文件写入到当前项目下
            try (InputStream inputStream = new ByteArrayInputStream(audioData)){
                String ttsFilePath = minioService.uploadFile(inputStream, "tts", objectName, "audio/mpeg", audioData.length);
                log.info("音频文件已保存到MinIO，文件路径为：{}", ttsFilePath);
                return ttsFilePath;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "存储失败,当前音频文件未生成或者不存在!";
    }

    // 语速标签 → rate 映射
    private static final Map<String, String> SPEED_MAP = Map.of(
            "快", "1.15",   // 原 1.25
            "正常", "1.08", // 原 1.10
            "慢", "0.98"    // 原 1.00
    );

    // 语气标签 → {rate, pitch} 映射
    private static final Map<String, String[]> TONE_MAP = Map.of(
            "温柔", new String[]{"1.04", "0.99"},
            "傲娇", new String[]{"1.08", "1.02"},
            "生气", new String[]{"1.12", "1.03"},
            "难过", new String[]{"0.99", "0.97"},
            "害羞", new String[]{"1.03", "1.01"},
            "平淡", new String[]{"1.06", "0.99"},
            "撒娇", new String[]{"1.04", "1.03"},
            "开心", new String[]{"1.10", "1.02"}
    );
    // 匹配 [语速:X] 或 [语气:X] 标签的正则
    private static final Pattern VOICE_TAG_PATTERN = Pattern.compile("\\[(语速|语气):([^\\]]+)\\]");

    private static String stripActionDesc(String text) {
        if (text == null) return null;
        // 1. 先移除完整括号对
        String result = ACTION_DESC_PATTERN.matcher(text).replaceAll("");
        // 2. 兜底：移除不闭合的左括号到行尾
        result = UNCLOSED_BRACKET_PATTERN.matcher(result).replaceAll("");
        // 3. 清理残留空括号
        result = EMPTY_BRACKET_PATTERN.matcher(result).replaceAll("");
        return result.trim();
    }

    /**
     * 解析文本中的语音标签，生成带有动态SSML参数的语音文本
     * @point1 这其中调整的三个参数分别为: 语速、音高、音量
     * @point2 支持 [语速:快/正常/慢] 和 [语气:温柔/傲娇/生气/难过/害羞/平淡/撒娇/开心]
     * @param text 含语音标签的原始文本
     * @return SSML格式文本
     */
    private String wrapWithSSML(String text) {
        // 如果文本中没有任何语音标签，直接用默认参数包装
        if (!VOICE_TAG_PATTERN.matcher(text).find()) {
            String escaped = escapeXml(text);
            return "<speak rate=\"" + DEFAULT_RATE + "\" pitch=\"" + DEFAULT_PITCH + "\" volume=\"" + DEFAULT_VOLUME + "\">" + escaped + "</speak>";
        }

        // 按语音标签拆分文本为多个片段，每个片段有独立的语速/语气参数
        StringBuilder ssml = new StringBuilder();
        Matcher matcher = VOICE_TAG_PATTERN.matcher(text);

        String currentRate = DEFAULT_RATE;
        String currentPitch = DEFAULT_PITCH;
        int lastEnd = 0;

        while (matcher.find()) {
            // 对于标签之前的文本的处理逻辑是给一个默认参数保持默认的音高声音和语速
            String before = text.substring(lastEnd, matcher.start());
            if (!before.trim().isEmpty()) {
                String escaped = escapeXml(before.trim());
                ssml.append("<speak rate=\"").append(currentRate)
                    .append("\" pitch=\"").append(currentPitch)
                    .append("\" volume=\"").append(DEFAULT_VOLUME)
                    .append("\">").append(escaped).append("</speak>");
            }

            // 修改语速和音高的默认参数
            String tagType = matcher.group(1); //匹配的第一个是语速
            String tagValue = matcher.group(2); //第二个被匹配的是音高
            if ("语速".equals(tagType)) {
                String mappedRate = SPEED_MAP.get(tagValue);
                if (mappedRate != null) {
                    currentRate = mappedRate;
                }
            } else if ("语气".equals(tagType)) {
                String[] toneParams = TONE_MAP.get(tagValue);
                if (toneParams != null) {
                    currentRate = toneParams[0];
                    currentPitch = toneParams[1];
                }
            }

            lastEnd = matcher.end();
        }

        // 处理最后一个标签之后的剩余文本
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd);
            if (!remaining.trim().isEmpty()) {
                String escaped = escapeXml(remaining.trim());
                ssml.append("<speak rate=\"").append(currentRate)
                    .append("\" pitch=\"").append(currentPitch)
                    .append("\" volume=\"").append(DEFAULT_VOLUME)
                    .append("\">").append(escaped).append("</speak>");
            }
        }

        String result = ssml.toString();
        // 如果解析后为空（标签被清除但没有实际文本），返回默认
        if (result.isEmpty()) {
            return "<speak rate=\"" + DEFAULT_RATE + "\" pitch=\"" + DEFAULT_PITCH + "\" volume=\"" + DEFAULT_VOLUME + "\">" + escapeXml(text.replaceAll(VOICE_TAG_PATTERN.pattern(), "").trim()) + "</speak>";
        }
        return result;
    }

    /**
     * 从AI回复中移除语音标签，返回纯文本（用于展示给用户）
     * @param text 含语音标签的文本
     * @return 纯文本
     */
    public static String stripVoiceTags(String text) {
        if (text == null) return null;
        return text.replaceAll("\\[(语速|语气):[^\\]]+\\]", "").trim();
    }

    /**
     * 转义XML特殊字符，防止SSML解析异常
     */
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
