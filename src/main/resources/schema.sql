-- 博客数据库初始化脚本
-- 思考：数据库设计的核心原则
-- 1. 范式设计：减少数据冗余，保证数据一致性
-- 2. 适当的反范式：为了查询性能，可以存储一些冗余数据（如评论数）
-- 3. 索引设计：为常用查询条件创建索引

-- 创建数据库
CREATE DATABASE IF NOT EXISTS myblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE myblog;

-- 文章表
-- 思考：字段设计考虑
-- 1. summary: 摘要字段，用于列表展示，避免查询大文本
-- 2. view_count/comment_count: 冗余字段，避免每次count查询
-- 3. status: 状态字段，支持草稿/发布两种状态
-- 4. 使用DATETIME而非TIMESTAMP：TIMESTAMP有2038年问题
CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content LONGTEXT NOT NULL COMMENT '文章内容(使用LONGTEXT支持长文)',
    summary VARCHAR(500) COMMENT '文章摘要',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    view_count INT DEFAULT 0 COMMENT '浏览量(冗余字段,由Redis同步)',
    comment_count INT DEFAULT 0 COMMENT '评论数(冗余字段)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=草稿, 1=已发布',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status_created (status, created_at) COMMENT '发布文章列表查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 评论表
-- 思考：评论功能的设计考虑
-- 1. parent_id: 支持评论回复（树形结构）
-- 2. email: 可选，用于回复通知
-- 3. status: 支持评论审核机制
-- 4. nickname: 不需要登录即可评论，记录昵称
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '关联文章ID',
    nickname VARCHAR(50) NOT NULL COMMENT '评论者昵称',
    email VARCHAR(100) COMMENT '评论者邮箱(可选)',
    content VARCHAR(1000) NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(0表示顶级评论)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=待审核, 1=已通过',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id) COMMENT '文章评论查询索引',
    INDEX idx_parent_id (parent_id) COMMENT '回复查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 插入测试数据
INSERT INTO article (title, content, summary, status) VALUES
('欢迎来到我的博客', '这是我的第一篇博客文章，欢迎使用这个博客系统。\n\n这个项目用于学习Spring Boot、MyBatis-Plus、Redis等技术栈。', '欢迎访问，这是第一篇文章', 1),
('Spring Boot学习笔记', '## Spring Boot简介\n\nSpring Boot简化了Spring应用的初始搭建和开发过程。\n\n### 核心特性\n\n1. 自动配置\n2. 起步依赖\n3. 内嵌服务器\n\n继续学习中...', 'Spring Boot核心特性介绍', 1);
