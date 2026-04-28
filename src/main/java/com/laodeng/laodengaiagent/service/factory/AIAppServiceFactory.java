package com.laodeng.laodengaiagent.service.factory;

import com.laodeng.laodengaiagent.service.AIAppService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/12
 * @description AI 应用服务工厂类
 */
@Log4j2
@Component
public class AIAppServiceFactory {

    private final Map<AIAppService.AppType, AIAppService> serviceMap;

    public AIAppServiceFactory(List<AIAppService> services) {
        this.serviceMap = new EnumMap<>(AIAppService.AppType.class);
        for (AIAppService service : services) {
            serviceMap.put(service.getAppType(), service);
        }
    }

    /**
     * 根据类型获取对应的服务实现
     *
     * @param appType 应用类型
     * @return 对应的服务实现
     */
    public AIAppService getService(AIAppService.AppType appType) {
        AIAppService service = serviceMap.get(appType);
        if (service == null) {
            throw new IllegalArgumentException("未找到对应的服务类型：" + appType);
        }
        return service;
    }
    /**
     * 获取恋爱应用服务
     * @return LoveAppServiceImpl
     */
    public AIAppService getLoveAppService() {
        return getService(AIAppService.AppType.LOVE);
    }

    /**
     * 获取图片应用服务
     * @return ImageAppServiceImpl
     */
    public AIAppService getImageAppService() {
        return getService(AIAppService.AppType.IMAGE);
    }

    /**
     * 获取 React 应用服务
     * @return ReactAgentServiceImpl
     */
    public AIAppService getReactAppService() {
        return getService(AIAppService.AppType.REACT);
    }
}
