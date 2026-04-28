package com.laodeng.laodengaiagent.event;

import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/13 18:18
 * @description 模型配置变更事件
 */

@Getter
public class ModelConfigChangedEvent extends ApplicationEvent {
    private final Long configId;
    private final String configKey;
    private final boolean deleted;
    public ModelConfigChangedEvent(Object source, Long configId, String configKey, boolean deleted) {
        super(source); //这里是Spring事件标准用于标注触发这个事件的实体类(例如ServiceImpl中的触发了可见得也就是会在那个类中的方法调用创建事件类的时候传入this也就是这个触发这个事件的类)
        this.configId = configId;
        this.configKey = configKey;
        this.deleted = deleted;
    }
}
