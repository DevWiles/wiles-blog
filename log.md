# 博客系统开发思考日志

本文档记录了开发过程中的设计决策和技术思考，方便学习理解。

---

## 小白入门指南：项目搭建顺序

### 我应该从哪里开始？

作为一个刚接触后端开发的小白，面对一个项目可能会感到无从下手。下面我按照**依赖关系**来说明搭建顺序：

```
第一步：基础设施（数据库、配置）
    ↓
第二步：公共组件（统一响应、异常处理）
    ↓
第三步：核心业务（文章模块）
    ↓
第四步：扩展功能（评论、统计）
    ↓
第五步：安全防护（登录认证）
```

### 为什么要按这个顺序？

#### 第一步：基础设施

**涉及文件**：
- `pom.xml` - 依赖配置
- `application.yaml` - 应用配置
- `schema.sql` - 数据库表结构

**为什么先做这个？**

就像盖房子要先打地基一样，没有这些基础配置，后面的代码都无法运行。

```
没有数据库连接 → Entity/Mapper无法工作
没有Redis配置 → 浏览统计功能无法实现
没有依赖包 → 代码编译都通不过
```

**学习要点**：
1. 理解Maven依赖管理：每个依赖包是做什么的？
2. 理解配置文件结构：Spring Boot如何读取配置？
3. 理解数据库设计：为什么表要这样设计？

---

#### 第二步：公共组件

**涉及文件**：
- `common/Result.java` - 统一响应格式
- `common/GlobalExceptionHandler.java` - 全局异常处理

**为什么第二步做这个？**

因为后面的所有Controller都要用到Result返回数据，都要依赖异常处理。先把公共组件准备好，后面写业务代码时就能直接使用。

```
如果先写Controller → 每个方法都要自己处理异常
如果先准备Result → 所有Controller统一使用，代码更简洁
```

**学习要点**：
1. 理解泛型：`Result<T>` 中的 `<T>` 是什么意思？
2. 理解注解：`@RestControllerAdvice` 是做什么的？
3. 理解统一规范：为什么要统一响应格式？

---

#### 第三步：核心业务 - 文章模块

**涉及文件**：
```
entity/Article.java       → 数据是什么样子的？
mapper/ArticleMapper.java → 怎么操作数据库？
service/ArticleService.java → 业务逻辑怎么处理？
controller/ArticleController.java → 怎么暴露API？
```

**为什么要按照 Entity → Mapper → Service → Controller 的顺序？**

这叫做**自底向上**的开发方式：

```
┌─────────────────────────────────────┐
│           Controller                │  ← 依赖 Service
│    (接收请求，返回响应)              │
├─────────────────────────────────────┤
│            Service                  │  ← 依赖 Mapper
│    (业务逻辑，调用多个Mapper)        │
├─────────────────────────────────────┤
│            Mapper                   │  ← 依赖 Entity
│    (数据库操作，MyBatis-Plus)        │
├─────────────────────────────────────┤
│            Entity                   │  ← 最底层，无依赖
│    (数据模型，与数据库表对应)         │
└─────────────────────────────────────┘
```

**为什么不能反过来？**

试试看如果先写Controller会怎样：

```java
@RestController
public class ArticleController {
    // 想调用 Service，但 Service 还没写
    // 想返回 Article，但 Entity 还没定义
    // 想操作数据库，但 Mapper 还没有
}
```

你会发现自己什么都写不了，因为缺少依赖。

**学习要点**：
1. Entity：理解数据库表和Java对象的映射关系
2. Mapper：理解MyBatis-Plus如何简化数据库操作
3. Service：理解业务逻辑应该放在哪里
4. Controller：理解RESTful API的设计原则

---

#### 第四步：扩展功能 - 评论和统计

**涉及文件**：
- `entity/Comment.java`, `mapper/CommentMapper.java`, ...
- `service/ViewCountService.java`

**为什么放第四步？**

评论功能依赖文章（评论要关联文章ID），所以先有文章模块。浏览统计依赖文章和Redis，所以要有Redis配置。

```
文章模块完成了
    ↓
可以在此基础上添加评论（评论需要关联文章）
    ↓
可以在此基础上添加统计（统计需要Redis）
```

这体现了**增量开发**的思想：先实现核心功能，再逐步扩展。

**学习要点**：
1. 理解表之间的关联关系（article_id外键）
2. 理解树形数据结构的设计（parent_id）
3. 理解Redis在高频读写场景的应用

---

#### 第五步：安全防护 - 登录认证

**涉及文件**：
- `interceptor/AuthInterceptor.java` - 登录拦截器
- `controller/AuthController.java` - 登录接口
- `config/WebConfig.java` - 注册拦截器

**为什么放最后？**

因为登录认证是**横切关注点**，它会拦截所有需要登录的请求。但是，在业务功能还没完成之前，先把认证加上反而会影响开发调试。

```
开发阶段：可以先不登录，直接测试API
开发完成后：加上认证，保护管理接口
```

**学习要点**：
1. 理解拦截器的工作原理
2. 理解Session认证机制
3. 理解如何区分公开接口和管理接口

---

### 完整搭建流程（小白版）

假设你现在是一个完全的小白，按照以下步骤操作：

#### 第1天：搭建基础环境

1. **安装必要软件**
   - JDK 21
   - MySQL 8.0
   - Redis
   - IDEA（或其他IDE）

2. **创建数据库**
   ```sql
   CREATE DATABASE myblog;
   USE myblog;
   -- 执行 schema.sql 创建表
   ```

3. **配置项目**
   - 修改 `application.yaml` 中的数据库密码
   - 确认 Redis 已启动

4. **运行项目**
   ```bash
   mvnw.cmd spring-boot:run
   ```
   看到 "Started MyblogApplication" 表示成功

#### 第2天：理解公共组件

1. 阅读 `Result.java`，理解统一响应格式
2. 阅读 `GlobalExceptionHandler.java`，理解异常处理
3. 用浏览器访问 `http://localhost:8080/api/articles`，观察返回格式

#### 第3天：理解文章模块

1. 按照 Entity → Mapper → Service → Controller 的顺序阅读代码
2. 每看一个文件，问自己：
   - 这个类是做什么的？
   - 它依赖哪些类？
   - 它被哪些类依赖？

3. 尝试用 curl 或 Postman 测试API：
   ```bash
   # 获取文章列表
   curl http://localhost:8080/api/articles

   # 登录
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

#### 第4天：理解评论和统计

1. 阅读评论模块代码，重点关注树形结构如何构建
2. 阅读浏览统计代码，理解Redis如何使用
3. 测试评论功能，观察返回的树形结构

#### 第5天：理解登录认证

1. 阅读拦截器代码，理解请求是如何被拦截的
2. 阅读认证控制器，理解登录流程
3. 尝试不登录直接POST文章，观察401错误

---

### 常见小白问题

**Q：为什么我的代码报红线（编译错误）？**

A：检查以下几项：
1. 依赖是否下载完成？（IDEA右下角进度条）
2. JDK是否正确配置？（File → Project Structure → SDK）
3. 是否缺少import语句？（鼠标移到红线处，Alt+Enter自动导入）

**Q：为什么启动报错 "Communications link failure"？**

A：数据库连接失败，检查：
1. MySQL是否启动？
2. 用户名密码是否正确？
3. 数据库myblog是否创建？

**Q：为什么Redis操作报错？**

A：检查Redis是否启动：
```bash
# Windows
redis-server

# 测试连接
redis-cli ping
# 返回 PONG 表示正常
```

**Q：我修改了代码，为什么不生效？**

A：需要重启应用。Spring Boot开发时，每次修改Java代码都需要重启。可以使用 spring-boot-devtools 实现热重载。

---

## 1. 项目架构设计

### 为什么选择三层架构？

```
Controller → Service → Mapper
```

**思考过程**：
- Controller层：只负责接收请求、参数校验、返回响应。保持简洁，不写业务逻辑。
- Service层：封装业务逻辑，可以调用多个Mapper组合复杂操作。可以使用`@Transactional`管理事务。
- Mapper层：数据访问层，MyBatis-Plus的`BaseMapper`已提供基础CRUD。

**好处**：
1. 职责清晰，便于维护
2. Service可以复用，不同Controller可以调用同一个Service
3. 便于单元测试，可以Mock各层

---

## 2. MyBatis-Plus vs 原生MyBatis

### 为什么选择MyBatis-Plus？

| 特性 | 原生MyBatis | MyBatis-Plus |
|------|------------|--------------|
| CRUD | 需要手写SQL | 继承BaseMapper即可 |
| 分页 | 手写分页SQL | 内置分页插件 |
| 条件查询 | 拼接SQL或XML | LambdaQueryWrapper链式调用 |
| 代码生成 | 需要配置 | 内置代码生成器 |

**LambdaQueryWrapper的优势**：
```java
// 类型安全，编译期检查字段名
wrapper.eq(Article::getStatus, 1)
       .orderByDesc(Article::getCreatedAt);

// 而不是硬编码字段名（容易写错）
wrapper.eq("status", 1);  // 字段名写错只有运行时才发现
```

---

## 3. 浏览量计数方案对比

### 为什么用Redis而不是直接写MySQL？

**方案对比**：

| 方案 | 优点 | 缺点 |
|------|------|------|
| 直接更新MySQL | 简单，数据实时一致 | 高并发时数据库压力大，每秒可能上千次UPDATE |
| Redis计数 + 定时同步 | 性能高，减轻DB压力 | 需要定时任务，Redis宕机可能丢数据 |
| 消息队列异步更新 | 解耦，可扩展 | 架构复杂，引入MQ运维成本 |

**当前选择**：Redis计数 + 定时同步（方案2）

**实现要点**：
```java
// Redis INCR是原子操作，线程安全
Long viewCount = redisTemplate.opsForValue().increment(key);

// 每5分钟同步到MySQL
@Scheduled(fixedRate = 5 * 60 * 1000)
public void syncViewCountToDatabase() { ... }
```

**生产环境优化点**：
1. 使用`SCAN`代替`KEYS`命令（KEYS会阻塞Redis）
2. 维护一个文章ID集合，避免扫描所有key
3. 考虑Redis持久化（AOF）防止数据丢失

---

## 4. 评论树形结构设计

### 如何高效获取树形评论？

**方案对比**：

| 方案 | 实现方式 | 优点 | 缺点 |
|------|----------|------|------|
| parent_id方式 | 每条评论存parent_id | 简单，适合浅层级 | 深层级需要递归查询 |
| path方式 | 存储路径如`1/3/5` | 查询高效 | 更新复杂，移动评论麻烦 |
| 闭包表 | 单独的关系表 | 查询最灵活 | 需要额外表，维护成本高 |

**博客场景**：评论层级通常较浅（2-3层），选择parent_id方式。

**优化技巧**：一次查询所有评论，内存中组装树
```java
// 第一遍：转为Map
for (Comment comment : comments) {
    map.put(comment.getId(), toVO(comment));
}

// 第二遍：建立父子关系
for (Comment comment : comments) {
    if (comment.getParentId() == 0) {
        roots.add(map.get(comment.getId()));
    } else {
        map.get(comment.getParentId()).getChildren().add(map.get(comment.getId()));
    }
}
```

时间复杂度O(n)，空间复杂度O(n)。

---

## 5. Session认证 vs JWT

### 为什么选择Session？

**Session方案**：
```
登录 → 服务端创建Session → 返回SessionId(Cookie)
访问 → 携带Cookie → 服务端查找Session → 验证身份
```

**优点**：
1. 实现简单，Spring Boot内置支持
2. 服务端可以主动让用户下线（删除Session）
3. 不需要额外学习JWT

**缺点**：
1. 多服务器部署需要Session共享（可以用Redis解决）
2. 服务器需要存储Session状态

**JWT方案**：
```
登录 → 服务端签发Token → 返回Token给客户端
访问 → 携带Token → 服务端验证签名 → 解析用户信息
```

**优点**：无状态，适合分布式系统
**缺点**：无法主动让用户下线，Token刷新复杂

**学习项目**：选择Session足够，生产环境推荐JWT。

---

## 6. 拦截器 vs 过滤器

### 登录校验为什么用拦截器？

| 对比项 | 过滤器(Filter) | 拦截器(Interceptor) |
|--------|----------------|---------------------|
| 所属层面 | Servlet层 | Spring MVC层 |
| 执行时机 | DispatcherServlet之前 | Controller前后 |
| 能否获取Handler | 否 | 能 |
| 依赖Spring | 否 | 是 |

**登录校验选择拦截器的原因**：
1. 可以获取Handler信息（知道是哪个Controller方法）
2. 更贴近业务逻辑
3. 可以注入Spring Bean（如UserService）

**实现要点**：
```java
public boolean preHandle(HttpServletRequest request, ...) {
    // GET请求放行（公开接口）
    if ("GET".equalsIgnoreCase(request.getMethod())) {
        return true;
    }
    // 检查Session
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute(LOGIN_USER_KEY) == null) {
        // 返回401未授权
        response.setStatus(401);
        return false;
    }
    return true;
}
```

---

## 7. 统一响应格式

### 为什么需要Result<T>包装？

**不统一的情况**：
```java
// 各种返回格式
return article;                    // 直接返回对象
return "success";                  // 返回字符串
throw new RuntimeException("错误"); // 抛异常
```

前端需要处理各种情况，代码混乱。

**统一后**：
```java
{
    "code": 200,
    "message": "success",
    "data": { ... }
}

{
    "code": 401,
    "message": "请先登录",
    "data": null
}
```

前端处理逻辑统一：
```javascript
if (response.code === 200) {
    // 成功，使用 response.data
} else {
    // 失败，显示 response.message
}
```

---

## 8. 参数校验

### 为什么用@Valid而不是手动校验？

**手动校验（不推荐）**：
```java
if (article.getTitle() == null || article.getTitle().isEmpty()) {
    return Result.error("标题不能为空");
}
if (article.getContent() == null || article.getContent().isEmpty()) {
    return Result.error("内容不能为空");
}
// ... 更多校验
```

**声明式校验（推荐）**：
```java
@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;
}

public Result<Article> create(@Valid @RequestBody ArticleRequest request) {
    // 校验失败会被 GlobalExceptionHandler 捕获
}
```

**好处**：
1. 代码简洁，校验规则和字段定义在一起
2. 复用性强，DTO可以在多处使用
3. 错误信息统一处理

---

## 9. 时间字段的选择

### 为什么用LocalDateTime而不是Date？

| 特性 | java.util.Date | java.time.LocalDateTime |
|------|----------------|-------------------------|
| 可变性 | 可变（线程不安全） | 不可变（线程安全） |
| API | 方法已废弃 | 现代API，方法命名清晰 |
| 时区处理 | 混乱 | 清晰（LocalDateTime无时区） |

**MyBatis-Plus自动填充**：
```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createdAt;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updatedAt;

// 配合 MetaObjectHandler 自动设置时间
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
}
```

---

## 10. 为什么用DTO而不是直接用Entity？

### Entity vs DTO

**Entity**：数据库映射，包含所有字段
```java
@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private Integer viewCount;    // 不应该让客户端设置
    private Integer commentCount;  // 不应该让客户端设置
    private LocalDateTime createdAt; // 自动填充
}
```

**DTO**：API交互，只包含需要的字段
```java
@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String summary;
    // 客户端不能设置 viewCount、createdAt 等字段
}
```

**好处**：
1. 安全性：防止客户端设置不应该修改的字段
2. 灵活性：Entity变化不影响API
3. 校验：可以在DTO上添加校验注解

---

## 总结

这个项目虽然简单，但涵盖了后端开发的很多核心概念：

1. **分层架构**：职责分离，便于维护
2. **ORM框架**：MyBatis-Plus简化数据库操作
3. **缓存设计**：Redis用于高频读写场景
4. **安全认证**：Session + 拦截器
5. **API设计**：RESTful风格，统一响应格式
6. **数据结构**：树形评论的组装算法

每个技术选型都有其权衡，没有最好的方案，只有最适合的方案。