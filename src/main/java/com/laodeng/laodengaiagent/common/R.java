package com.laodeng.laodengaiagent.common;

import cn.hutool.http.HttpStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/18 11:18
 * @description 相应通用类
 */
@Data
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code = NORMAL_CODE;

    private String msg = "";

    private T data;

    public static final String SUCCESS = "操作成功";

    public static final String FAILURE = "操作失败";

    public static final Integer ERROR_CODE = HttpStatus.HTTP_INTERNAL_ERROR;

    public static final Integer NORMAL_CODE = HttpStatus.HTTP_OK;

    public R() {

    }

    public R(T data) {
        this.data = data;
    }
    public R(String msg) {
        this.msg = msg;
    }

    public R(Integer code, T data, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return ok(NORMAL_CODE, SUCCESS, null);
    }

    public static <T> R<T> ok(Integer code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok(T data) {
        return ok(NORMAL_CODE, SUCCESS, data);
    }

    public static <T> R<T> fail(Integer code, String msg) {
        return ok(code, msg, null);
    }

    public static <T> R<T> fail(String msg) {
        return ok(ERROR_CODE, msg, null);
    }

    public static <T> R<T> fail() {
        return ok(ERROR_CODE, FAILURE, null);
    }
}
