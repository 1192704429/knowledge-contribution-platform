package com.kp.mall.module.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.blog.entity.Blog;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 博客Service接口
 */
public interface BlogService extends IService<Blog> {

    /**
     * 分页查询
     */
    IPage<Blog> pageQuery(Integer current, Integer size, Blog entity);

    /**
     * 列表查询（不分页）
     */
    List<Blog> listQuery(Blog entity);

    /**
     * 根据ID查询
     */
    Blog getById(Long id);

    /**
     * 下拉数据：blog_category
     */
    List<SelectVO> listBlogCategory();

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

    /**
     * 下拉数据：admin
     */
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    IPage<Blog> pageQueryForUser(Integer current, Integer size, Blog entity);

    /**
     * 博文审核通过时发放积分：每次15分，每日上限2次
     *
     * @param userId 发布用户ID
     * @param blogId 博文ID
     */
    void grantPointsForBlogApproved(Long userId, Long blogId);

}
