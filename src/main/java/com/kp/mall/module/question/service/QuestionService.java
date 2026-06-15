package com.kp.mall.module.question.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.question.entity.Question;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 问答Service接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 分页查询
     */
    IPage<Question> pageQuery(Integer current, Integer size, Question entity);

    /**
     * 列表查询（不分页）
     */
    List<Question> listQuery(Question entity);

    /**
     * 根据ID查询
     */
    Question getById(Long id);

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

    /**
     * 下拉数据：qa_category
     */
    List<SelectVO> listQaCategory();

    /**
     * 下拉数据：admin
     */
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    IPage<Question> pageQueryForUser(Integer current, Integer size, Question entity);

    /**
     * 问答审核通过时发放积分：每次10分，每日上限3次
     */
    void grantPointsForQuestionApproved(Long userId, Long questionId);

}
