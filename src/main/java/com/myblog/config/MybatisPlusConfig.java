package com.myblog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 *
 * 思考：MyBatis-Plus相比原生MyBatis的优势
 * 1. 内置CRUD接口：继承BaseMapper即可拥有常用增删改查方法
 * 2. 分页插件：无需手写分页SQL
 * 3. 代码生成器：可根据数据库表自动生成Entity、Mapper、Service等
 * 4. 条件构造器：链式调用构建复杂查询条件
 *
 * @MapperScan：扫描Mapper接口所在包，生成代理实现类
 */
@Configuration
@MapperScan("com.myblog.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件配置
     *
     * 思考：为什么需要分页？
     * 1. 数据量大时，一次性查询所有数据会占用大量内存
     * 2. 前端展示需要分页，提升用户体验
     * 3. 减少网络传输数据量
     *
     * MyBatis-Plus分页插件会自动拦截分页查询，
     * 根据数据库类型生成对应的分页SQL（如MySQL的LIMIT）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
