package com.kp.mall.module.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.blog.entity.Blog;
import com.kp.mall.module.blog.mapper.BlogMapper;
import com.kp.mall.module.blog.service.BlogService;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author wuming
 * @description: 博客Service实现类
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    /** 积分类型：1-获取 */
    private static final int INTEGRAL_TYPE_GET = 1;
    /** 积分来源：2-博文审核通过 */
    private static final int INTEGRAL_SOURCE_BLOG_APPROVED = 2;
    /** 博文审核通过奖励积分 */
    private static final int BLOG_APPROVED_POINTS = 15;
    /** 博文审核通过每日上限次数 */
    private static final int BLOG_APPROVED_DAILY_CAP = 2;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private IntegralRecordService integralRecordService;
    @Autowired
    private UserService userService;

    @Override
    public IPage<Blog> pageQuery(Integer current, Integer size, Blog entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Blog> records = blogMapper.pageQuery(entity, offset, limit);
        Long total = blogMapper.countQuery(entity);

        Page<Blog> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Blog> listQuery(Blog entity) {
        return blogMapper.listQuery(entity);
    }

    @Override
    public Blog getById(Long id) {
        return blogMapper.getById(id);
    }

    /**
     * 下拉数据：blog_category
     */
    @Override
    public List<SelectVO> listBlogCategory() {
        return blogMapper.listBlogCategory();
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return blogMapper.listUser();
    }

    /**
     * 下拉数据：admin
     */
    @Override
    public List<SelectVO> listAdmin() {
        return blogMapper.listAdmin();
    }

    /**
     * 用户端分页查询（按发布时间排序）
     */
    @Override
    public IPage<Blog> pageQueryForUser(Integer current, Integer size, Blog entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Blog> records = blogMapper.pageQueryForUser(entity, offset, limit);
        Long total = blogMapper.countQueryForUser(entity);

        Page<Blog> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public void grantPointsForBlogApproved(Long userId, Long blogId) {
        if (userId == null || blogId == null) {
            return;
        }
        LocalDateTime todayStart = LocalDate.now(ZONE_ID).atStartOfDay();
        long countToday = integralRecordService.count(new LambdaQueryWrapper<IntegralRecord>()
                .eq(IntegralRecord::getUserId, userId)
                .eq(IntegralRecord::getIntegralSource, INTEGRAL_SOURCE_BLOG_APPROVED)
                .ge(IntegralRecord::getCreateTime, todayStart));
        if (countToday >= BLOG_APPROVED_DAILY_CAP) {
            return;
        }
        User user = userService.getById(userId);
        if (user == null) {
            return;
        }
        int currentIntegral = user.getTotalIntegral() != null ? user.getTotalIntegral() : 0;
        int newBalance = currentIntegral + BLOG_APPROVED_POINTS;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setTotalIntegral(newBalance);
        userService.updateById(updateUser);

        Blog blog = getById(blogId);
        String titlePart = (blog != null && blog.getTitle() != null)
                ? (blog.getTitle().length() > 50 ? blog.getTitle().substring(0, 50) + "…" : blog.getTitle())
                : "";
        String relatedDesc = "博文审核通过-博文ID:" + blogId + (titlePart.isEmpty() ? "" : "-标题:" + titlePart) + "-获得" + BLOG_APPROVED_POINTS + "积分";

        IntegralRecord record = new IntegralRecord();
        record.setUserId(userId);
        record.setIntegralType(INTEGRAL_TYPE_GET);
        record.setIntegralSource(INTEGRAL_SOURCE_BLOG_APPROVED);
        record.setIntegralNum(BLOG_APPROVED_POINTS);
        record.setRelatedId(blogId);
        record.setRelatedDesc(relatedDesc);
        record.setBalance(newBalance);
        record.setCreateTime(LocalDateTime.now());
        integralRecordService.save(record);
    }

}
