package com.kp.mall.module.blog_category.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.blog_category.entity.BlogCategory;

import java.util.List;

/**
 * @author wuming
 * @description: 博客分类Service接口
 */
public interface BlogCategoryService extends IService<BlogCategory> {

    /**
     * 分页查询
     */
    IPage<BlogCategory> pageQuery(Integer current, Integer size, BlogCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<BlogCategory> listQuery(BlogCategory entity);

    /**
     * 根据ID查询
     */
    BlogCategory getById(Long id);

}
