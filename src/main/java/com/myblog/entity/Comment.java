package com.myblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * 思考：评论功能的设计
 * 1. 支持匿名评论：不需要登录，记录昵称即可
 * 2. 支持回复：通过parent_id关联父评论
 * 3. 支持审核：status字段标记审核状态
 *
 * 思考：为什么允许匿名评论？
 * 1. 降低评论门槛，鼓励用户互动
 * 2. 博客系统通常不需要严格的用户体系
 * 3. 但可能面临垃圾评论问题，需要审核机制
 */
@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的文章ID
     */
    private Long articleId;

    /**
     * 评论者昵称
     */
    private String nickname;

    /**
     * 评论者邮箱（可选，用于回复通知）
     */
    private String email;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
     * 思考：如何设计树形评论结构？
     * 方案1：parent_id方式（当前方案）
     *   - 优点：简单，适合层级少的场景
     *   - 缺点：深层次查询需要递归或多次查询
     * 方案2：path方式（存储完整路径，如 1/3/5）
     *   - 优点：查询高效，适合深层级
     *   - 缺点：实现复杂
     * 方案3：闭包表
     *   - 优点：查询最灵活
     *   - 缺点：需要额外表，维护成本高
     *
     * 博客评论通常层级较浅（2-3层），使用parent_id即可
     */
    private Long parentId;

    /**
     * 状态：0=待审核，1=已通过
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
