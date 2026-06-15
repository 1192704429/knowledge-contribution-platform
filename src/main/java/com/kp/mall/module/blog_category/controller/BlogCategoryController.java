package com.kp.mall.module.blog_category.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.blog_category.entity.BlogCategory;
import com.kp.mall.module.blog_category.service.BlogCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 博客分类Controller
 */
@RestController
@RequestMapping("/blog_category")
public class BlogCategoryController {

    @Autowired
    private BlogCategoryService blogCategoryService;

    /**
     * 分页查询博客分类列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) BlogCategory entity) {
        if (entity == null) {
            entity = new BlogCategory();
        }
        return Result.success(blogCategoryService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有博客分类列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) BlogCategory entity) {
        if (entity == null) {
            entity = new BlogCategory();
        }
        List<BlogCategory> list = blogCategoryService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询博客分类
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        BlogCategory blogCategory = blogCategoryService.getById(id);
        if (blogCategory == null) {
            return Result.error("博客分类不存在");
        }
        return Result.success(blogCategory);
    }

    /**
     * 新增博客分类
     */
    @PostMapping
    public Result save(@RequestBody BlogCategory blogCategory) {
        blogCategory.setCreateTime(LocalDateTime.now());
        boolean success = blogCategoryService.save(blogCategory);
        if (success) {
            return Result.success("新增成功", blogCategory);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新博客分类
     */
    @PutMapping
    public Result update(@RequestBody BlogCategory blogCategory) {
        boolean success = blogCategoryService.updateById(blogCategory);
        if (success) {
            return Result.success("更新成功", blogCategory);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除博客分类
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = blogCategoryService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除博客分类
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = blogCategoryService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
