package com.myblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.common.Result;
import com.myblog.entity.Article;
import com.myblog.service.ArticleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 *
 * 思考：RESTful API设计原则
 * 1. 使用HTTP动词表示操作：GET查询、POST创建、PUT更新、DELETE删除
 * 2. URL表示资源，使用名词而非动词：/articles 而非 /createArticle
 * 3. 使用HTTP状态码表示结果：200成功、400参数错误、404未找到等
 *
 * 思考：参数校验
 * 1. @Valid：触发Bean校验
 * 2. @NotBlank等注解：声明式校验，代码更简洁
 * 3. 校验失败会被GlobalExceptionHandler捕获处理
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 文章创建请求DTO
     * 思考：为什么要用DTO而不是直接用Entity？
     * 1. Entity是数据库映射，不应该暴露给前端
     * 2. DTO可以控制哪些字段可以被客户端设置
     * 3. 可以添加额外的校验逻辑
     */
    @Data
    public static class ArticleRequest {
        @NotBlank(message = "标题不能为空")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        private String summary;
        private String coverImage;
        private Integer status = 1; // 默认发布
    }

    /**
     * 获取文章列表（分页）
     * 公开接口，无需登录
     */
    @GetMapping
    public Result<Page<Article>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        // 思考：分页参数的限制
        // 防止恶意请求大量数据
        if (pageSize > 100) {
            pageSize = 100;
        }

        Page<Article> page = articleService.list(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取文章详情
     * 公开接口，无需登录
     * 每次访问会增加浏览量
     */
    @GetMapping("/{id}")
    public Result<Article> get(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.notFound("文章不存在");
        }
        return Result.success(article);
    }

    /**
     * 创建文章
     * 需要登录（由拦截器控制）
     */
    @PostMapping
    public Result<Article> create(@Valid @RequestBody ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());

        Article created = articleService.create(article);
        return Result.success(created);
    }

    /**
     * 更新文章
     * 需要登录
     */
    @PutMapping("/{id}")
    public Result<Article> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());

        Article updated = articleService.update(id, article);
        if (updated == null) {
            return Result.notFound("文章不存在");
        }
        return Result.success(updated);
    }

    /**
     * 删除文章
     * 需要登录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = articleService.delete(id);
        if (!success) {
            return Result.notFound("文章不存在");
        }
        return Result.success();
    }
}
