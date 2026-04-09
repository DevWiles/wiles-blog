package com.myblog.common;

import lombok.Data;

/**
 * 统一API响应封装
 *
 * 思考：为什么要统一响应格式？
 * 1. 前端处理逻辑一致，减少判断代码
 * 2. 便于统一处理异常和错误
 * 3. 方便添加通用字段（如时间戳、traceId等）
 *
 * 实际项目可能使用更复杂的结构，这里保持简单
 */
@Data
public class Result<T> {
    /**
     * 状态码：200成功，其他失败
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据载荷
     */
    private T data;

    // 成功响应
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败响应
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // 常用错误码
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> Result<T> unauthorized() {
        return error(401, "请先登录");
    }

    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> Result<T> serverError(String message) {
        return error(500, message);
    }
}
