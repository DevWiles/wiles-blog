package com.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.entity.Comment;
import com.myblog.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论服务层
 *
 * 思考：评论功能的实现要点
 * 1. 支持树形结构：通过parent_id关联父评论
 * 2. 支持分页：避免评论过多时加载缓慢
 * 3. 评论数同步：新增评论时更新文章的comment_count
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleService articleService;

    /**
     * 获取文章的评论列表（树形结构）
     *
     * 思考：如何高效获取树形评论？
     * 方案1：递归查询（不推荐，N+1问题）
     * 方案2：一次查询所有评论，内存中组装树（当前方案）
     *   - 适合评论数量不多的场景
     *   - 博客文章评论通常不会太多，这个方案足够
     * 方案3：使用CTE递归查询（MySQL 8.0+）
     *   - 数据库层面解决，性能好
     *   - 但可读性较差
     */
    public List<CommentVO> getTreeByArticleId(Long articleId) {
        // 查询该文章所有已审核的评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, 1)  // 已审核
                .orderByAsc(Comment::getCreatedAt);

        List<Comment> comments = commentMapper.selectList(wrapper);

        // 转换为VO并在内存中组装树形结构
        return buildTree(comments);
    }

    /**
     * 构建评论树
     *
     * 思考：树形结构组装算法
     * 1. 先将所有评论转为VO，用Map存储（id -> VO）
     * 2. 遍历所有评论，找到其父评论，添加到父评论的children中
     * 3. 最终只返回顶级评论（parentId = 0）
     *
     * 时间复杂度O(n)，空间复杂度O(n)
     */
    private List<CommentVO> buildTree(List<Comment> comments) {
        Map<Long, CommentVO> map = new HashMap<>();
        List<CommentVO> roots = new ArrayList<>();

        // 第一遍遍历：转为VO并存入Map
        for (Comment comment : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setNickname(comment.getNickname());
            vo.setEmail(comment.getEmail());
            vo.setContent(comment.getContent());
            vo.setParentId(comment.getParentId());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            map.put(comment.getId(), vo);
        }

        // 第二遍遍历：建立父子关系
        for (Comment comment : comments) {
            CommentVO vo = map.get(comment.getId());
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                // 顶级评论
                roots.add(vo);
            } else {
                // 子评论，找到父评论并添加
                CommentVO parent = map.get(comment.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        return roots;
    }

    /**
     * 发表评论
     *
     * 思考：评论功能的安全性考虑
     * 1. XSS攻击：过滤HTML标签或转义
     * 2. 垃圾评论：可以添加验证码或审核机制
     * 3. 频率限制：防止刷评论
     * 这里简化处理，实际项目需要更多安全措施
     */
    public Comment create(Comment comment) {
        // 设置默认状态为已审核（简化处理）
        // 实际项目中可能默认为待审核(0)
        comment.setStatus(1);

        // 如果parentId为空，设置为0表示顶级评论
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        commentMapper.insert(comment);

        // 更新文章评论数
        articleService.incrementCommentCount(comment.getArticleId());

        return comment;
    }

    /**
     * 评论VO（视图对象）
     * 思考：VO的作用
     * 1. 控制返回给前端的字段
     * 2. 添加额外字段（如children）
     * 3. 与Entity解耦，Entity变化不影响API
     */
    @lombok.Data
    public static class CommentVO {
        private Long id;
        private String nickname;
        private String email;
        private String content;
        private Long parentId;
        private String createdAt;
        private List<CommentVO> children;
    }
}
