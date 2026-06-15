package com.kp.mall.module.blog.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.common.Result;
import com.kp.mall.module.blog.entity.Blog;
import com.kp.mall.module.blog.service.BlogService;
import com.kp.mall.module.blog_category.entity.BlogCategory;
import com.kp.mall.module.blog_category.service.BlogCategoryService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author wuming
 * @description: 用户端博客Controller
 */
@RestController
@RequestMapping("/user/blog")
public class UserBlogController {

    @Autowired
    private BlogService blogService;
    
    @Autowired
    private BlogCategoryService blogCategoryService;
    
    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;

    /**
     * 博文列表页：分页查询博文（用户端）
     * <p>
     * 请求体示例：{ "current": 1, "size": 10, "categoryId": 1, "title": "关键词" }
     * - current: 页码，默认 1
     * - size: 每页条数，默认 10
     * - categoryId: 可选，按分类筛选
     * - title: 可选，标题模糊搜索
     * </p>
     * 只返回状态正常且审核通过的博文，按发布时间倒序。
     * 响应 data 为 IPage：records、total、size、current。
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        Blog entity = new Blog();
        Integer current = 1;
        Integer size = 10;
        
        if (params != null) {
            // 提取分页参数
            if (params.get("current") != null) {
                current = Integer.valueOf(params.get("current").toString());
            }
            if (params.get("size") != null) {
                size = Integer.valueOf(params.get("size").toString());
            }
            // 提取查询条件
            if (params.get("categoryId") != null) {
                entity.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
            }
            if (params.get("title") != null) {
                entity.setTitle(params.get("title").toString());
            }
        }
        
        // 用户端只显示状态正常且审核通过的博客
        entity.setStatus(1);
        entity.setAuditStatus(1);
        return Result.success(blogService.pageQueryForUser(current, size, entity));
    }

    /**
     * 我的博文列表（个人中心）：分页查询当前用户发布的博文（含待审核、驳回等）
     * 请求体：{ "current": 1, "size": 10, "title": "", "auditStatus": null }
     */
    @PostMapping("/my")
    public Result myList(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Blog entity = new Blog();
        entity.setUserId(userId);
        Integer current = 1;
        Integer size = 10;
        if (params != null) {
            if (params.get("current") != null) current = Integer.valueOf(params.get("current").toString());
            if (params.get("size") != null) size = Integer.valueOf(params.get("size").toString());
            if (params.get("title") != null && !params.get("title").toString().trim().isEmpty()) {
                entity.setTitle(params.get("title").toString().trim());
            }
            if (params.get("auditStatus") != null) entity.setAuditStatus(Integer.valueOf(params.get("auditStatus").toString()));
        }
        return Result.success(blogService.pageQueryForUser(current, size, entity));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser u = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return u != null ? u.getId() : null;
    }

    /**
     * 获取当前用户自己的博文（用于编辑回显），不校验审核状态，不增加阅读量
     */
    @GetMapping("/my/{id}")
    public Result getMyBlogForEdit(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.error("博文不存在");
        }
        if (!userId.equals(blog.getUserId())) {
            return Result.error("无权编辑该博文");
        }
        return Result.success(blog);
    }

    /**
     * 根据ID查询博客详情（用户端）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        // 检查博客状态和审核状态
        if (blog.getStatus() == null || blog.getStatus() != 1) {
            return Result.error("博客不存在或已删除");
        }
        if (blog.getAuditStatus() == null || blog.getAuditStatus() != 1) {
            return Result.error("博客未通过审核");
        }
        // 进入详情页增加阅读量
        int newReadCount = (blog.getReadCount() == null ? 0 : blog.getReadCount()) + 1;
        Blog updateRead = new Blog();
        updateRead.setId(blog.getId());
        updateRead.setReadCount(newReadCount);
        blogService.updateById(updateRead);
        blog.setReadCount(newReadCount);
        return Result.success(blog);
    }

    /**
     * 博文列表页：获取博文分类列表（用户端）
     * <p>
     * 用于左侧分类栏，只返回状态正常的分类。响应 data 为 List&lt;BlogCategory&gt;。
     * </p>
     */
    @GetMapping("/category/list")
    public Result listCategory() {
        BlogCategory category = new BlogCategory();
        category.setStatus(1); // 只查询状态正常的分类
        List<BlogCategory> list = blogCategoryService.listQuery(category);
        return Result.success(list);
    }
    
    /**
     * 用户发布博客
     */
    @PostMapping
    public Result publishBlog(@RequestBody Blog blog, HttpServletRequest request) {
        // 验证token并获取用户ID
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        
        // 验证token
        boolean verifiedToken = jwtUtil.verifyToken(authorization);
        if (!verifiedToken) {
            return Result.error(401, "登录状态已过期~");
        }
        
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        
        // 设置用户ID
        blog.setUserId(loginUser.getId());
        
        // 验证必填字段
        if (StrUtil.isEmpty(blog.getTitle())) {
            return Result.error("博客标题不能为空");
        }
        if (StrUtil.isEmpty(blog.getContent())) {
            return Result.error("博客内容不能为空");
        }
        if (blog.getCategoryId() == null) {
            return Result.error("请选择博客分类");
        }
        
        // 设置默认值
        blog.setCreateTime(LocalDateTime.now());
        blog.setPublishTime(LocalDateTime.now());
        blog.setStatus(1); // 正常状态
        blog.setAuditStatus(0); // 待审核
        blog.setReadCount(0); // 初始阅读量为0
        
        // 自动生成博客编号：BL + 时间戳（精确到毫秒）
        if (StrUtil.isEmpty(blog.getBlogCode())) {
            String blogCode = "BL" + System.currentTimeMillis();
            blog.setBlogCode(blogCode);
        }
        
        boolean success = blogService.save(blog);
        if (success) {
            return Result.success("发布成功，等待审核", blog);
        }
        return Result.error("发布失败");
    }
    
    /**
     * 用户更新博客
     */
    @PutMapping
    public Result updateBlog(@RequestBody Blog blog, HttpServletRequest request) {
        // 验证token并获取用户ID
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        
        // 验证token
        boolean verifiedToken = jwtUtil.verifyToken(authorization);
        if (!verifiedToken) {
            return Result.error(401, "登录状态已过期~");
        }
        
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        
        // 检查博客是否存在
        Blog existingBlog = blogService.getById(blog.getId());
        if (existingBlog == null) {
            return Result.error("博客不存在");
        }
        
        // 检查是否是当前用户的博客
        if (!loginUser.getId().equals(existingBlog.getUserId())) {
            return Result.error("无权修改此博客");
        }
        
        // 检查是否已审核通过，已审核通过的不能修改
        if (existingBlog.getAuditStatus() != null && existingBlog.getAuditStatus() == 1) {
            return Result.error("已审核通过的博客不能修改");
        }
        
        // 验证必填字段
        if (StrUtil.isEmpty(blog.getTitle())) {
            return Result.error("博客标题不能为空");
        }
        if (StrUtil.isEmpty(blog.getContent())) {
            return Result.error("博客内容不能为空");
        }
        if (blog.getCategoryId() == null) {
            return Result.error("请选择博客分类");
        }
        
        // 更新后重置审核状态为待审核
        blog.setAuditStatus(0);
        
        boolean success = blogService.updateById(blog);
        if (success) {
            return Result.success("更新成功，等待审核", blog);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 用户删除博客
     */
    @DeleteMapping("/{id}")
    public Result deleteBlog(@PathVariable Long id, HttpServletRequest request) {
        // 验证token并获取用户ID
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        
        // 验证token
        boolean verifiedToken = jwtUtil.verifyToken(authorization);
        if (!verifiedToken) {
            return Result.error(401, "登录状态已过期~");
        }
        
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        
        // 检查博客是否存在
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        
        // 检查是否是当前用户的博客
        if (!loginUser.getId().equals(blog.getUserId())) {
            return Result.error("无权删除此博客");
        }
        
        // 检查是否已审核通过，已审核通过的不能删除
        if (blog.getAuditStatus() != null && blog.getAuditStatus() == 1) {
            return Result.error("已审核通过的博客不能删除");
        }
        
        boolean success = blogService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
