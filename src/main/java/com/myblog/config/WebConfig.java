package com.myblog.config;

import com.myblog.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * 思考：拦截器 vs 过滤器
 * 1. 过滤器(Filter)：Servlet层面的，在请求到达DispatcherServlet之前执行
 * 2. 拦截器(Interceptor)：Spring MVC层面的，在Controller前后执行
 * 3. 对于登录校验，使用拦截器更合适，可以获取Handler信息
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 注册拦截器
     *
     * 思考：拦截路径的设计
     * 1. 管理接口需要登录：POST/PUT/DELETE操作
     * 2. 公开接口不需要登录：GET查询操作
     * 3. 登录接口本身需要排除
     *
     * 注意：由于GET请求在拦截器中已经做了放行处理，
     * 这里只需要排除登录相关接口即可
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有文章相关接口
                .addPathPatterns("/api/articles/**")
                // 排除登录相关接口
                .excludePathPatterns(
                        "/api/auth/**"
                );
    }
}
