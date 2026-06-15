package com.kp.mall.module.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.blog.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 博客Mapper接口
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * 分页查询
     */
    List<Blog> pageQuery(@Param("entity") Blog entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Blog entity);

    /**
     * 列表查询（不分页）
     */
    List<Blog> listQuery(@Param("entity") Blog entity);

    /**
     * 根据ID查询
     */
    Blog getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：blog_category
     */
    @Select("select category_name as label, id as value from blog_category")
    List<SelectVO> listBlogCategory();

    /**
     * 下拉数据：user
     */
    @Select("select user_name as label, id as value from user")
    List<SelectVO> listUser();

    /**
     * 下拉数据：admin
     */
    @Select("select admin_name as label, id as value from admin")
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    List<Blog> pageQueryForUser(@Param("entity") Blog entity,
                                 @Param("offset") Long offset,
                                 @Param("limit") Long limit);

    /**
     * 用户端查询总数
     */
    Long countQueryForUser(@Param("entity") Blog entity);

}
