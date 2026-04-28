package com.laodeng.laodengaiagent.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/13 19:15
 * @description
 */

@Data
@Accessors(chain = true)
public class AiModelConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String configKey;

    private String provider;

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Double temperature;

    private Boolean enabled;

    private String completionsPath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
