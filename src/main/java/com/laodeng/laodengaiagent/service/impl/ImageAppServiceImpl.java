package com.laodeng.laodengaiagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.laodeng.laodengaiagent.app.ImageApp;
import com.laodeng.laodengaiagent.service.AIAppService;
import com.laodeng.laodengaiagent.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/12 11:21
 * @description
 */

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class ImageAppServiceImpl implements AIAppService {

    private final ImageApp imageApp;
    private final UserService userService;

    @Override
    public AppType getAppType() {
        return AppType.IMAGE;
    }

    /**
     * 图片内容理解分析
     * @param userMessageInsert 用户提问信息
     * @param picturePath 图片路径（MinIO对象名称或URL）
     * @return ContentUnderstand 图片内容理解结果
     */
    public ImageApp.ContentUnderstand contextLoads(String userMessageInsert, String picturePath) {
        ImageApp.ContentUnderstand contentUnderstand = imageApp.promptChat(userMessageInsert, userService.getById(StpUtil.getLoginIdAsLong()).getUsername(), picturePath);
        log.info("最终图片识别结果: {}", contentUnderstand);
        return contentUnderstand;

    }

}
