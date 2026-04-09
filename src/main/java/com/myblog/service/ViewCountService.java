package com.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myblog.entity.Article;
import com.myblog.mapper.ArticleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 浏览统计服务
 *
 * 思考：为什么用Redis做浏览量计数？
 * 1. 高性能：Redis是内存数据库，读写极快
 * 2. 原子操作：INCR命令是原子的，不用担心并发问题
 * 3. 减轻数据库压力：避免每次浏览都更新数据库
 *
 * 思考：为什么要定时同步到MySQL？
 * 1. Redis数据可能丢失（未开启AOF或RDB）
 * 2. 数据库是持久化的最终存储
 * 3. 同步频率可以根据业务需求调整
 *
 * 思考：其他方案
 * 1. 消息队列：浏览事件发送到MQ，异步消费更新
 * 2. 写时更新：达到一定阈值时更新数据库
 * 当前方案：定时任务同步，简单可靠
 */
@Service
public class ViewCountService {

    private static final Logger log = LoggerFactory.getLogger(ViewCountService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Redis中文章浏览量的key前缀
     */
    private static final String VIEW_COUNT_KEY = "article:view:";

    /**
     * 增加文章浏览量
     *
     * 思考：Redis INCR命令的特点
     * 1. 如果key不存在，会先初始化为0，再执行INCR
     * 2. 返回值是增加后的结果
     * 3. 是原子操作，线程安全
     */
    public Long incrementViewCount(Long articleId) {
        String key = VIEW_COUNT_KEY + articleId;
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 获取文章在Redis中的浏览量
     */
    public Integer getViewCount(Long articleId) {
        String key = VIEW_COUNT_KEY + articleId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 定时同步浏览量到数据库
     *
     * 思考：@Scheduled注解的使用
     * fixedRate：固定频率执行，不管上一次是否执行完
     * fixedDelay：上一次执行完成后，等待指定时间再执行
     * cron：使用cron表达式，更灵活
     *
     * 这里使用fixedRate = 5分钟，即每5分钟同步一次
     * 实际项目中可能需要根据业务调整：
     * - 热门博客：频繁同步
     * - 冷门博客：可以降低频率
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 每5分钟执行
    public void syncViewCountToDatabase() {
        log.info("开始同步浏览量到数据库...");

        // 扫描所有浏览量key
        // 思考：KEYS命令在生产环境中慎用，会阻塞Redis
        // 更好的方案是使用SCAN命令，或者单独维护一个文章ID集合
        // 这里为了简化使用KEYS，实际项目建议改进
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的浏览量数据");
            return;
        }

        int syncCount = 0;
        for (String key : keys) {
            try {
                // 解析文章ID
                Long articleId = Long.parseLong(key.substring(VIEW_COUNT_KEY.length()));

                // 获取Redis中的浏览量
                String countStr = redisTemplate.opsForValue().get(key);
                if (countStr == null) {
                    continue;
                }
                Integer viewCount = Integer.parseInt(countStr);

                // 更新数据库
                Article article = articleMapper.selectById(articleId);
                if (article != null) {
                    article.setViewCount(viewCount);
                    articleMapper.updateById(article);
                    syncCount++;
                }
            } catch (Exception e) {
                log.error("同步浏览量失败: key={}", key, e);
            }
        }

        log.info("浏览量同步完成，共同步 {} 篇文章", syncCount);
    }

    /**
     * 初始化文章浏览量到Redis
     * 用于系统启动时，将数据库中的浏览量同步到Redis
     *
     * 思考：为什么需要初始化？
     * 如果Redis重启或数据丢失，需要从数据库恢复
     */
    public void initViewCountToRedis(Long articleId, Integer viewCount) {
        String key = VIEW_COUNT_KEY + articleId;
        redisTemplate.opsForValue().set(key, String.valueOf(viewCount));
    }
}
