package com.laodeng.laodengaiagent.service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/9 09:31
 * @description
 */

public interface InfluxDBService {

    void recordApiRequest(String endpoint, String method, long durationMs, int statusCode);

    void recordAgentExecution(String agentName, String memoryId, long durationMs, int toolCallCount, boolean success);

    void recordToolCall(String toolName, long durationMs, Boolean success);

}
