# MyBlog - 个人博客系统

一个基于 Spring Boot 的个人博客系统，用于学习 Spring Boot、MyBatis-Plus、Redis 等技术栈。

## 功能特性

- 文章管理：发布、编辑、删除文章
- 评论系统：支持树形评论、匿名评论
- 浏览统计：基于 Redis 的高性能浏览计数
- 简单认证：Session 登录，保护管理接口

## 技术栈

- Java 21
- Spring Boot 4.0.5
- MyBatis-Plus 3.5.9
- MySQL 8.0
- Redis
- Lombok

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis

### 1. 克隆项目

```bash
git clone <repository-url>
cd myblog
```

### 2. 创建数据库

连接 MySQL 执行：

```sql
CREATE DATABASE IF NOT EXISTS myblog DEFAULT CHARACTER SET utf8mb4;
```

或直接执行项目中的 SQL 脚本：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/myblog?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的密码
```

### 4. 启动 Redis

```bash
# Windows
redis-server

# Linux/Mac
redis-server
```

### 5. 启动应用

**Windows**：
```bash
mvnw.cmd spring-boot:run
```

**Linux/Mac**：
```bash
./mvnw spring-boot:run
```

启动成功后访问：http://localhost:8080

### 6. 测试 API

**登录获取Session**：
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -c cookies.txt
```

**创建文章（需要登录）**：
```bash
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"title":"测试文章","content":"这是测试内容"}'
```

**获取文章列表（无需登录）**：
```bash
curl http://localhost:8080/api/articles
```

**获取文章详情（无需登录）**：
```bash
curl http://localhost:8080/api/articles/1
```

**发表评论（无需登录）**：
```bash
curl -X POST http://localhost:8080/api/articles/1/comments \
  -H "Content-Type: application/json" \
  -d '{"nickname":"访客","content":"写得不错！"}'
```

## API 接口

| 方法 | 路径 | 描述 | 需要登录 |
|------|------|------|----------|
| GET | /api/articles | 文章列表（分页） | 否 |
| GET | /api/articles/{id} | 文章详情 | 否 |
| POST | /api/articles | 创建文章 | 是 |
| PUT | /api/articles/{id} | 更新文章 | 是 |
| DELETE | /api/articles/{id} | 删除文章 | 是 |
| GET | /api/articles/{id}/comments | 评论列表 | 否 |
| POST | /api/articles/{id}/comments | 发表评论 | 否 |
| POST | /api/auth/login | 登录 | 否 |
| POST | /api/auth/logout | 登出 | 否 |
| GET | /api/auth/check | 检查登录状态 | 否 |

## 管理员账号

默认账号：`admin`
默认密码：`admin123`

可在 `application.yaml` 中修改：

```yaml
admin:
  username: admin
  password: admin123
```

## 项目结构

```
src/main/java/com/myblog/
├── config/           # 配置类
│   ├── MybatisPlusConfig.java
│   ├── WebConfig.java
│   └── MyMetaObjectHandler.java
├── controller/       # 控制器层
│   ├── ArticleController.java
│   ├── CommentController.java
│   └── AuthController.java
├── service/          # 服务层
│   ├── ArticleService.java
│   ├── CommentService.java
│   └── ViewCountService.java
├── mapper/           # 数据访问层
│   ├── ArticleMapper.java
│   └── CommentMapper.java
├── entity/           # 实体类
│   ├── Article.java
│   └── Comment.java
├── common/           # 公共类
│   ├── Result.java
│   └── GlobalExceptionHandler.java
├── interceptor/      # 拦截器
│   └── AuthInterceptor.java
└── MyblogApplication.java
```

## 学习资源

开发过程中的设计思考记录在 [log.md](log.md) 中，包括：

- 架构设计决策
- 技术选型对比
- 常见问题解决方案

## 常见问题

### 启动失败：数据库连接错误

1. 确认 MySQL 已启动
2. 检查数据库连接配置是否正确
3. 确认数据库 `myblog` 已创建

### 启动失败：Redis连接错误

1. 确认 Redis 已启动
2. 如果 Redis 设置了密码，在 `application.yaml` 中配置：
   ```yaml
   spring:
     data:
       redis:
         password: your_redis_password
   ```

### 中文乱码

确保数据库和表的字符集为 `utf8mb4`，连接字符串包含 `characterEncoding=utf-8`。

## License

MIT
