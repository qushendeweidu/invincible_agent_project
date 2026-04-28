package com.laodeng.laodengaiagent.domain.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/13 19:03
 * @description
 */

@Data
@Accessors(chain = true)
public class AiModelConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // 模型标签
    private String configKey;
    // 模型供应商
    private String provider;
    // 模型服务地址
    private String baseUrl;
    // 模型密钥
    private String apiKey;
    // 模型名称
    private String modelName;
    // 模型温度
    private Double temperature;
    // 模型是否启用
    private Boolean enabled;
    // 模型调用路径
    private String completionsPath;


}
