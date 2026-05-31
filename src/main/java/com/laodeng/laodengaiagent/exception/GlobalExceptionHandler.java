package com.laodeng.laodengaiagent.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenContextException;
import com.laodeng.laodengaiagent.common.ErrorCode;
import com.laodeng.laodengaiagent.common.R;
import com.laodeng.laodengaiagent.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public R<?> notLoginExceptionHandler(NotLoginException e) {
        log.error("未登录: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "未登录或 token 已过期");
    }

    @ExceptionHandler(NotRoleException.class)
    public R<?> notRoleExceptionHandler(NotRoleException e) {
        log.error("无角色权限: {}", e.getRole());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无角色权限: " + e.getRole());
    }

    @ExceptionHandler(NotPermissionException.class)
    public R<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("无操作权限: {}", e.getPermission());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无操作权限: " + e.getPermission());
    }

    @ExceptionHandler(SaTokenContextException.class)
    public R<?> saTokenContextExceptionHandler(SaTokenContextException e) {
        log.error("权限异常: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "权限异常: " + e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public R<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

}
