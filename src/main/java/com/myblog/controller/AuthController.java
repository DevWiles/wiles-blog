package com.myblog.controller;

import com.myblog.common.Result;
import com.myblog.interceptor.AuthInterceptor;
import com.myblog.controller.AuthController.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 思考：简单登录方案的设计
 * 1. 管理员账号密码配置在application.yaml中
 * 2. 使用Session存储登录状态
 * 3. 不涉及数据库用户表，保持简单
 *
 * 实际项目中应该：
 * 1. 密码加密存储（BCrypt）
 * 2. 支持多用户和角色权限
 * 3. 可能需要验证码、防暴力破解等
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 登录接口
     *
     * 思考：Session的工作流程
     * 1. 调用request.getSession()，如果没有Session会创建一个
     * 2. Session默认存储在内存中，重启应用会丢失
     * 3. 可以配置Redis存储Session，实现Session共享
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        // 验证用户名密码
        if (adminUsername.equals(request.getUsername())
                && adminPassword.equals(request.getPassword())) {
            // 登录成功，将用户信息存入Session
            session.setAttribute(AuthInterceptor.LOGIN_USER_KEY, request.getUsername());
            return Result.success("登录成功");
        }

        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 登出接口
     *
     * 思考：Session的销毁
     * session.invalidate() 会销毁当前Session
     * 同时清除Cookie中的JSESSIONID
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出登录");
    }

    /**
     * 检查登录状态
     *
     * 思考：为什么需要这个接口？
     * 1. 页面刷新时检查是否仍处于登录状态
     * 2. 前端根据登录状态显示/隐藏管理功能
     */
    @GetMapping("/check")
    public Result<Boolean> checkLogin(HttpSession session) {
        Object user = session.getAttribute(AuthInterceptor.LOGIN_USER_KEY);
        return Result.success(user != null);
    }
}
