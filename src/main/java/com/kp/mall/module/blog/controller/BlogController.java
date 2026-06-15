package com.kp.mall.module.blog.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.blog.entity.Blog;
import com.kp.mall.module.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 博客Controller
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 分页查询博客列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Blog entity) {
        if (entity == null) {
            entity = new Blog();
        }
        return Result.success(blogService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有博客列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Blog entity) {
        if (entity == null) {
            entity = new Blog();
        }
        List<Blog> list = blogService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询博客
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        return Result.success(blog);
    }

    /**
     * 新增博客
     */
    @PostMapping
    public Result save(@RequestBody Blog blog) {
        blog.setCreateTime(LocalDateTime.now());
        // 自动生成博客编号：BL + 时间戳（精确到毫秒）
        if (blog.getBlogCode() == null || blog.getBlogCode().isEmpty()) {
            String blogCode = "BL" + System.currentTimeMillis();
            blog.setBlogCode(blogCode);
        }
        boolean success = blogService.save(blog);
        if (success) {
            return Result.success("新增成功", blog);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新博客
     */
    @PutMapping
    public Result update(@RequestBody Blog blog) {
        boolean success = blogService.updateById(blog);
        if (success) {
            return Result.success("更新成功", blog);
        }
        return Result.error("更新失败");
    }

    /**
     * 审核博客
     */
    @PutMapping("/audit")
    public Result audit(@RequestBody Blog blog) {
        Blog existBlog = blogService.getById(blog.getId());
        if (existBlog == null) {
            return Result.error("博客不存在");
        }
        // 只能审核待审核状态的博客
        if (existBlog.getAuditStatus() != null && existBlog.getAuditStatus() != 0) {
            return Result.error("只能审核待审核状态的博客");
        }
        // 审核驳回时，审核备注不能为空
        if (blog.getAuditStatus() != null && blog.getAuditStatus() == 2) {
            if (blog.getAuditRemark() == null || blog.getAuditRemark().trim().isEmpty()) {
                return Result.error("审核驳回时，审核备注不能为空");
            }
        }
        // 设置审核信息
        existBlog.setAuditStatus(blog.getAuditStatus());
        existBlog.setAuditRemark(blog.getAuditRemark());
        existBlog.setAuditAdminId(blog.getAuditAdminId());
        existBlog.setAuditTime(LocalDateTime.now());
        boolean success = blogService.updateById(existBlog);
        if (success) {
            // 审核通过时发放积分：每次15分，每日上限2次
            if (existBlog.getAuditStatus() != null && existBlog.getAuditStatus() == 1) {
                blogService.grantPointsForBlogApproved(existBlog.getUserId(), existBlog.getId());
            }
            return Result.success("审核成功", existBlog);
        }
        return Result.error("审核失败");
    }

    /**
     * 根据ID删除博客
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = blogService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除博客
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = blogService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：blog_category
     */
    @GetMapping("/select/listBlogCategory")
    public Result listBlogCategory() {
        List<SelectVO> list = blogService.listBlogCategory();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：user
     */
    @GetMapping("/select/listUser")
    public Result listUser() {
        List<SelectVO> list = blogService.listUser();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：admin
     */
    @GetMapping("/select/listAdmin")
    public Result listAdmin() {
        List<SelectVO> list = blogService.listAdmin();
        return Result.success(list);
    }

}
