package com.laodeng.laodengaiagent.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.laodeng.laodengaiagent.service.InfluxDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/9 09:47
 * @description
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class InfluxDBServiceImpl implements InfluxDBService {
    private final InfluxDBClient influxDBClient;

    @Override
    public void recordApiRequest(String endpoint, String method, long durationMs, int statusCode) {
        try {
            Point point = Point.measurement("api_request")
                    .addTag("endpoint", endpoint)
                    .addTag("method", method)
                    .addTag("status_code", String.valueOf(statusCode))
                    .addField("duration_ms", durationMs)
                    .time(Instant.now(), WritePrecision.MS);
            getWriteApi().writePoint(point);
        } catch (Exception e) {
            log.warn("ai请求监测记录失败: {}", e.getMessage());
        }

    }

    @Override
    public void recordAgentExecution(String agentName, String memoryId, long durationMs, int toolCallCount, boolean success) {
        try {
            Point point = Point.measurement("agent_execution")
                    .addTag("agent", agentName)
                    .addTag("memory_id", memoryId)
                    .addTag("success", String.valueOf(success))
                    .addField("duration_ms", durationMs)
                    .addField("tool_call_count", toolCallCount)
                    .time(Instant.now(), WritePrecision.MS);
            getWriteApi().writePoint(point);
        } catch (Exception e) {
            log.warn("智能体调用监测记录失败: {}", e.getMessage());
        }
    }

    @Override
    public void recordToolCall(String toolName, long durationMs, Boolean success) {
        try {
            Point point = Point.measurement("tool_call")
                    .addTag("tool", toolName)
                    .addTag("success", String.valueOf(success))
                    .addField("duration_ms", durationMs)
                    .time(Instant.now(), WritePrecision.MS);
            getWriteApi().writePoint(point);
        } catch (Exception e) {
            log.warn("工具调用监测记录失败: {}", e.getMessage());
        }
    }

    private WriteApiBlocking getWriteApi() {
        return influxDBClient.getWriteApiBlocking();
    }
}
