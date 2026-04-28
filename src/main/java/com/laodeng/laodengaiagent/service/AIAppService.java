package com.laodeng.laodengaiagent.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/12 11:20
 * @description
 */

public interface AIAppService {

    /**
     * 获取应用类型
     * @return AppType 应用类型枚举值
     */
    AppType getAppType();

    /**
     * AI应用类型枚举
     */
    enum AppType {
        /** 恋爱应用 */
        LOVE,
        /** 图片分析应用 */
        IMAGE,
        /** 多智能体应用 */
        REACT
    }

}
