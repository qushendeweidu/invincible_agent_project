package com.laodeng.laodengaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laodeng.laodengaiagent.domain.dto.AiModelConfigDTO;
import com.laodeng.laodengaiagent.domain.po.AiModelConfig;
import com.laodeng.laodengaiagent.domain.vo.AiModelConfigVO;

import java.util.List;
import java.util.Set;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/27 11:28
 * @description
 */


public interface AiModelConfigService extends IService<AiModelConfig> {
    /**
     * 更新模型配置并发布事件
     * @param entity 模型配置实体
     * @return boolean 更新结果
     */
    boolean updateByIdWithEvent(AiModelConfig entity);

    /**
     * 删除模型配置并发布事件
     * @param id 模型配置ID
     * @return boolean 删除结果
     */
    boolean removeByIdWithEvent(Long id);

    /**
     * 添加AI模型配置
     * @param aiModelConfigDTO 模型配置信息
     * @return boolean 添加结果
     */
    boolean addModelConfig(AiModelConfigDTO aiModelConfigDTO);

    /**
     * 更新AI模型配置
     * @param aiModelConfigDTO 模型配置信息
     * @return boolean 更新结果
     */
    boolean updateModelConfig(AiModelConfigDTO aiModelConfigDTO);

    /**
     * 获取所有AI模型配置列表
     * @return List<AiModelConfigVO> 模型配置列表
     */
    List<AiModelConfigVO> getAllConfig();

    /**
     * 根据ID获取AI模型配置
     * @param id 模型配置ID
     * @return AiModelConfigVO 模型配置信息
     */
    AiModelConfigVO getConfigsById(Long id);

    /**
     * 获取当前数据库中所有的模型配置的key(未被启用的不会被获取到)
     *
     * @return Set<String> 模型配置的key列表
     */
    Set<String> getAllModelKeys();
}
