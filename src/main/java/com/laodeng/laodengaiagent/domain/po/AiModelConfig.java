package com.laodeng.laodengaiagent.domain.po;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/27 10:47
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("ai_model_config")
public class AiModelConfig implements Serializable {
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
