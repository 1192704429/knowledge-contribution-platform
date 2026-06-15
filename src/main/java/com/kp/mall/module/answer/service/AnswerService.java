package com.kp.mall.module.answer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.answer.entity.Answer;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 问答回答Service接口
 */
public interface AnswerService extends IService<Answer> {

    /**
     * 分页查询
     */
    IPage<Answer> pageQuery(Integer current, Integer size, Answer entity);

    /**
     * 列表查询（不分页）
     */
    List<Answer> listQuery(Answer entity);

    /**
     * 根据ID查询
     */
    Answer getById(Long id);

    /**
     * 下拉数据：question
     */
    List<SelectVO> listQuestion();

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

    /**
     * 下拉数据：admin
     */
    List<SelectVO> listAdmin();

    /**
     * 审核通过时：调用 AI 评判回答质量（优秀/良好/其他），更新 answerGrade，并按规则发放积分。
     * 优秀=20分，良好=15分（每日上限5次），其他=0分。
     */
    void approveAndGrade(Long answerId);

    /**
     * 异步执行 AI 评级：审核通过后不阻塞接口，在后台调用 AI，成功后更新回答的 answerGrade（已评级）并发放积分。
     */
    void approveAndGradeAsync(Long answerId);

}
