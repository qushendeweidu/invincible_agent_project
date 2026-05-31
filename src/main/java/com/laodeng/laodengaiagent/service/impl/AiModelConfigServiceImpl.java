package com.laodeng.laodengaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laodeng.laodengaiagent.domain.dto.AiModelConfigDTO;
import com.laodeng.laodengaiagent.domain.po.AiModelConfig;
import com.laodeng.laodengaiagent.domain.vo.AiModelConfigVO;
import com.laodeng.laodengaiagent.event.ModelConfigChangedEvent;
import com.laodeng.laodengaiagent.mapper.AiModelConfigMapper;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import com.laodeng.laodengaiagent.service.AiModelConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/27 11:29
 * @description
 */

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig> implements AiModelConfigService {
    private final ApplicationEventPublisher publisher;

    /**
     * 更新模型配置并发布事件
     * @param entity 模型配置实体
     * @return 更新结果
     */
    @Override
    public boolean updateByIdWithEvent(AiModelConfig entity) {
        boolean result = this.updateById(entity);
        if (result) {
            publisher.publishEvent(new ModelConfigChangedEvent(this, entity.getId(), entity.getConfigKey(), false));
        }
        return result;
    }

    /**
     * 删除模型配置并发布事件
     * @param id 模型配置ID
     * @return 删除结果
     */
    @Override
    public boolean removeByIdWithEvent(Long id) {
        AiModelConfig config = this.getById(id);
        if (config == null) return false;

        boolean result = this.removeById(id);
        if (result) {
            publisher.publishEvent(new ModelConfigChangedEvent(this, id, config.getConfigKey(), true));
        }
        return result;
    }

    /**
     * 添加模型配置
     * @param aiModelConfigDTO 模型配置DTO
     * @return 添加结果
     */
    @Override
    public boolean addModelConfig(AiModelConfigDTO aiModelConfigDTO) {
        AiModelConfig aiModelConfig = BeanUtil.copyProperties(aiModelConfigDTO, AiModelConfig.class);
        return this.save(aiModelConfig);
    }

    /**
     * 修改模型配置
     * @param aiModelConfigDTO 模型配置DTO
     * @return 修改结果
     */
    @Override
    public boolean updateModelConfig(AiModelConfigDTO aiModelConfigDTO) {
        AiModelConfig aiModelConfig = BeanUtil.copyProperties(aiModelConfigDTO, AiModelConfig.class);
        return this.updateByIdWithEvent(aiModelConfig);
    }

    /**
     * 获取所有模型配置
     * @return 模型配置列表
     */
    @Override
    public List<AiModelConfigVO> getAllConfig() {
        List<AiModelConfig> aiModelConfigs = this.list();
        return BeanUtil.copyToList(aiModelConfigs, AiModelConfigVO.class);
    }

    @Override
    public AiModelConfigVO getConfigsById(Long id) {
        AiModelConfig aiModelConfig = this.getById(id);
        return BeanUtil.copyProperties(aiModelConfig, AiModelConfigVO.class);
    }

    /**
     * 获取当前数据库中所有的模型配置的key(未被启用的不会被获取到)
     *
     * @return Set<String> 模型配置的key列表
     */
    @Override
    public Set<String> getAllModelKeys(){
        Set<String> modelKey = this.getAllConfig().stream()
                .filter(AiModelConfigVO::getEnabled)
                .map(AiModelConfigVO::getConfigKey)
                .collect(Collectors.toSet());
        log.info("获取所有模型配置的key: {}", modelKey);
        return modelKey;
    }

}
