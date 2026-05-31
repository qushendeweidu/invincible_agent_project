package com.laodeng.laodengaiagent.annotition;

import java.lang.annotation.*;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/7 00:00
 * @description
 */

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolMetadata {
    boolean readOnly() default false;       // 只读工具（如 readPdf）
    boolean concurrencySafe() default false; // 可并行执行
    boolean destructive() default false;     // 不可逆操作
    int maxResultChars() default 3000;       // 输出截断限制
}
