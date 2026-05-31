package com.laodeng.laodengaiagent.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/7 10:50
 * @description
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamEvent {
    /**
     * 事件类型：start 模型开始运行 |
     * thinking 思考中 |
     * tool_call 工具调用中 |
     * tool_result 工具调用结果 |
     * text 工具子智能体或者著智能体的String类型的回答 |
     * error 执行过程中出现的异常 |
     * done 执行结束 |
     */
    private String type;

    /**
     * 事件内容数据
     */
    private String data;

    /**
     * 时间戳（毫秒）
     */
    private long timestamp;

    public static StreamEvent of(String type, String data) {
        return StreamEvent.builder()
                .type(type)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
