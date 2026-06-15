package com.kp.mall.module.blog_category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.blog_category.entity.BlogCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 博客分类Mapper接口
 */
@Mapper
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {

    /**
     * 分页查询
     */
    List<BlogCategory> pageQuery(@Param("entity") BlogCategory entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") BlogCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<BlogCategory> listQuery(@Param("entity") BlogCategory entity);

    /**
     * 根据ID查询
     */
    BlogCategory getById(@Param("id") Long id);

}
