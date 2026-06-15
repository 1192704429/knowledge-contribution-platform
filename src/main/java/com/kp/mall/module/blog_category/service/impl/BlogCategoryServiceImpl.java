package com.kp.mall.module.blog_category.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.blog_category.entity.BlogCategory;
import com.kp.mall.module.blog_category.mapper.BlogCategoryMapper;
import com.kp.mall.module.blog_category.service.BlogCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 博客分类Service实现类
 */
@Service
public class BlogCategoryServiceImpl extends ServiceImpl<BlogCategoryMapper, BlogCategory> implements BlogCategoryService {

    @Autowired
    private BlogCategoryMapper blogCategoryMapper;

    @Override
    public IPage<BlogCategory> pageQuery(Integer current, Integer size, BlogCategory entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<BlogCategory> records = blogCategoryMapper.pageQuery(entity, offset, limit);
        Long total = blogCategoryMapper.countQuery(entity);

        Page<BlogCategory> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<BlogCategory> listQuery(BlogCategory entity) {
        return blogCategoryMapper.listQuery(entity);
    }

    @Override
    public BlogCategory getById(Long id) {
        return blogCategoryMapper.getById(id);
    }

}
