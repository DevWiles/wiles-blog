package com.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.entity.Article;
import com.myblog.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文章服务层
 *
 * 思考：Service层的作用
 * 1. 封装业务逻辑，Controller只负责接收请求和返回响应
 * 2. 可以在Service中处理事务（@Transactional）
 * 3. 可以调用多个Mapper，组合复杂业务
 *
 * 思考：为什么使用LambdaQueryWrapper？
 * 1. 类型安全：字段名写错会在编译时报错
 * 2. 代码可读性好：链式调用
 * 3. 避免硬编码字段名
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ViewCountService viewCountService;

    /**
     * 分页查询文章列表
     *
     * 思考：分页查询的优化点
     * 1. 列表页只查询摘要，不查询content（大文本）
     * 2. 使用索引优化排序（status + created_at）
     * 3. 可以添加缓存，减少数据库压力
     */
    public Page<Article> list(int pageNum, int pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 只查询已发布的文章
        wrapper.eq(Article::getStatus, 1);
        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedAt);

        return articleMapper.selectPage(page, wrapper);
    }

    /**
     * 获取文章详情
     *
     * 思考：浏览量计数的设计
     * 方案1：直接更新数据库
     *   - 缺点：高并发时数据库压力大
     * 方案2：使用Redis计数，定时同步到数据库（当前方案）
     *   - 优点：Redis的INCR是原子操作，性能高
     *   - 缺点：需要定时任务同步，Redis宕机可能丢数据
     */
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return null;
        }

        // 增加浏览量（通过ViewCountService使用Redis计数）
        Long viewCount = viewCountService.incrementViewCount(id);

        // 更新到article对象中返回
        // 思考：这里返回Redis中的计数，而不是数据库值
        // 实际场景中可能需要：数据库值 + Redis增量
        article.setViewCount(viewCount.intValue());

        return article;
    }

    /**
     * 创建文章
     */
    public Article create(Article article) {
        // 如果没有摘要，自动截取内容前200字符
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String content = article.getContent();
            if (content != null && content.length() > 200) {
                article.setSummary(content.substring(0, 200) + "...");
            } else {
                article.setSummary(content);
            }
        }

        // 初始浏览量和评论数
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getCommentCount() == null) {
            article.setCommentCount(0);
        }

        articleMapper.insert(article);

        // 初始化Redis中的浏览量为0
        viewCountService.initViewCountToRedis(article.getId(), 0);

        return article;
    }

    /**
     * 更新文章
     */
    public Article update(Long id, Article article) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            return null;
        }

        // 更新字段
        existing.setTitle(article.getTitle());
        existing.setContent(article.getContent());
        existing.setSummary(article.getSummary());
        existing.setCoverImage(article.getCoverImage());
        existing.setStatus(article.getStatus());

        articleMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除文章
     */
    public boolean delete(Long id) {
        return articleMapper.deleteById(id) > 0;
    }

    /**
     * 增加评论数
     * 思考：这里直接更新数据库，评论操作频率低于浏览
     */
    public void incrementCommentCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setCommentCount(article.getCommentCount() + 1);
            articleMapper.updateById(article);
        }
    }
}
