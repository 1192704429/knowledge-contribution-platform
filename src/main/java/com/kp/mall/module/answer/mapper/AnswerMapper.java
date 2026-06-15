package com.kp.mall.module.answer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.answer.entity.Answer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 问答回答Mapper接口
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    /**
     * 分页查询
     */
    List<Answer> pageQuery(@Param("entity") Answer entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Answer entity);

    /**
     * 列表查询（不分页）
     */
    List<Answer> listQuery(@Param("entity") Answer entity);

    /**
     * 根据ID查询
     */
    Answer getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：question
     */
    @Select("select title as label, id as value from question")
    List<SelectVO> listQuestion();

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

}
