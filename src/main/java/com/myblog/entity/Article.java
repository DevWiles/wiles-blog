package com.myblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * 思考：MyBatis-Plus注解说明
 * @TableName：指定表名，默认类名转下划线（Article -> article）
 * @TableId：主键字段
 *   - type = IdType.AUTO：数据库自增
 *   - type = IdType.ASSIGN_ID：雪花算法生成（默认）
 * @TableField：普通字段
 *   - exist = false：非数据库字段
 *   - fill = FieldFill.INSERT：插入时自动填充
 *
 * 思考：时间字段的选择
 * java.util.Date vs java.time.LocalDateTime
 * 1. LocalDateTime是Java 8引入的新API，不可变、线程安全
 * 2. Date是可变对象，多线程下不安全
 * 3. 新项目推荐使用LocalDateTime
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容（支持Markdown）
     */
    private String content;

    /**
     * 文章摘要（用于列表展示）
     */
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 浏览量
     * 思考：为什么需要冗余这个字段？
     * 1. 避免每次count查询comment表
     * 2. 可以通过Redis实时更新，定时同步到数据库
     */
    private Integer viewCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 状态：0=草稿，1=已发布
     */
    private Integer status;

    /**
     * 创建时间
     * fill = FieldFill.INSERT：插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * fill = FieldFill.INSERT_UPDATE：插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
