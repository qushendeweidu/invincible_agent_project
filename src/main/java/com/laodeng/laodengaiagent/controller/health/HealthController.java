package com.laodeng.laodengaiagent.controller.health;

import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/15 19:57
 * @description 健康检查
 */
@RestController
@RequestMapping("/health")
public class HealthController {


    /**
     * 健康检查接口
     * @return String 返回 "ok" 表示服务正常
     */
    @SaCheckRole(value = "admin")
    @GetMapping
    public String healthCheck(){
        return "ok";
    }

}
