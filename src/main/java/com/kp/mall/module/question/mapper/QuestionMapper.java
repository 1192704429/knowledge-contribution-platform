package com.kp.mall.module.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 问答Mapper接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询
     */
    List<Question> pageQuery(@Param("entity") Question entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Question entity);

    /**
     * 列表查询（不分页）
     */
    List<Question> listQuery(@Param("entity") Question entity);

    /**
     * 根据ID查询
     */
    Question getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：user
     */
    @Select("select user_name as label, id as value from user")
    List<SelectVO> listUser();

    /**
     * 下拉数据：qa_category
     */
    @Select("select category_name as label, id as value from qa_category")
    List<SelectVO> listQaCategory();

    /**
     * 下拉数据：admin
     */
    @Select("select admin_name as label, id as value from admin")
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    List<Question> pageQueryForUser(@Param("entity") Question entity,
                                     @Param("offset") Long offset,
                                     @Param("limit") Long limit);

    /**
     * 用户端查询总数
     */
    Long countQueryForUser(@Param("entity") Question entity);

}
