package com.myblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客应用启动类
 *
 * 思考：@SpringBootApplication注解的作用
 * 这是一个组合注解，包含：
 * 1. @Configuration：标记为配置类
 * 2. @EnableAutoConfiguration：启用自动配置
 * 3. @ComponentScan：自动扫描组件
 *
 * 思考：@EnableScheduling注解的作用
 * 启用Spring的定时任务功能，配合@Scheduled注解使用
 * 浏览量同步任务需要这个注解才能生效
 */
@SpringBootApplication
@EnableScheduling
public class MyblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyblogApplication.class, args);
    }

}
