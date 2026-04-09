package com.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myblog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章Mapper接口
 *
 * 思考：MyBatis-Plus的BaseMapper提供了什么？
 * 继承BaseMapper后，自动拥有以下方法：
 * - insert(T entity)：插入
 * - deleteById(Serializable id)：根据ID删除
 * - updateById(T entity)：根据ID更新
 * - selectById(Serializable id)：根据ID查询
 * - selectList(Wrapper<T> queryWrapper)：条件查询列表
 * - selectPage(Page<T> page, Wrapper<T> queryWrapper)：分页查询
 * ... 等等
 *
 * 如果需要自定义SQL，可以：
 * 1. 在接口中定义方法
 * 2. 在resources/mapper/ArticleMapper.xml中编写SQL
 * 或者使用@Select等注解直接在方法上写SQL
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    // BaseMapper已经提供了基础CRUD方法
    // 如果需要自定义SQL，可以在这里添加方法声明
}
