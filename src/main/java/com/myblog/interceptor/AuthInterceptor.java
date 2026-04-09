package com.myblog.interceptor;

import com.myblog.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 *
 * 思考：拦截器的工作原理
 * 1. 请求到达Controller之前，先经过拦截器的preHandle方法
 * 2. 返回true：放行，继续执行Controller
 * 3. 返回false：拦截，直接返回响应
 *
 * 思考：Session认证的原理
 * 1. 用户登录后，服务端创建Session，返回SessionId（通过Cookie）
 * 2. 后续请求携带Cookie中的SessionId
 * 3. 服务端根据SessionId找到对应的Session，验证用户身份
 *
 * 思考：Session vs JWT
 * Session：
 *   - 优点：简单，服务端可以主动让用户下线
 *   - 缺点：多服务器部署需要Session共享
 * JWT：
 *   - 优点：无状态，适合分布式系统
 *   - 缺点：无法主动让用户下线，token刷新复杂
 *
 * 学习项目使用Session即可，生产环境推荐JWT
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * Session中存储登录用户的key
     */
    public static final String LOGIN_USER_KEY = "loginUser";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截非GET请求（管理操作需要登录）
        // GET请求由WebConfig的excludePathPatterns处理
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 检查Session中是否有登录信息
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(LOGIN_USER_KEY) == null) {
            // 未登录，返回401错误
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Result<Void> result = Result.unauthorized();
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        return true;
    }
}
