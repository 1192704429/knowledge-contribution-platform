package com.kp.mall.module.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.question.entity.Question;
import com.kp.mall.module.question.mapper.QuestionMapper;
import com.kp.mall.module.question.service.QuestionService;
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
 * @description: 问答Service实现类
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    /** 积分类型：1-获取 */
    private static final int INTEGRAL_TYPE_GET = 1;
    /** 积分来源：6-问答发布审核通过 */
    private static final int INTEGRAL_SOURCE_QUESTION_APPROVED = 6;
    /** 问答审核通过奖励积分 */
    private static final int QUESTION_APPROVED_POINTS = 10;
    /** 问答审核通过每日上限次数 */
    private static final int QUESTION_APPROVED_DAILY_CAP = 3;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private IntegralRecordService integralRecordService;
    @Autowired
    private UserService userService;

    @Override
    public IPage<Question> pageQuery(Integer current, Integer size, Question entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Question> records = questionMapper.pageQuery(entity, offset, limit);
        Long total = questionMapper.countQuery(entity);

        Page<Question> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Question> listQuery(Question entity) {
        return questionMapper.listQuery(entity);
    }

    @Override
    public Question getById(Long id) {
        return questionMapper.getById(id);
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return questionMapper.listUser();
    }

    /**
     * 下拉数据：qa_category
     */
    @Override
    public List<SelectVO> listQaCategory() {
        return questionMapper.listQaCategory();
    }

    /**
     * 下拉数据：admin
     */
    @Override
    public List<SelectVO> listAdmin() {
        return questionMapper.listAdmin();
    }

    /**
     * 用户端分页查询（按发布时间排序）
     */
    @Override
    public IPage<Question> pageQueryForUser(Integer current, Integer size, Question entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Question> records = questionMapper.pageQueryForUser(entity, offset, limit);
        Long total = questionMapper.countQueryForUser(entity);

        Page<Question> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public void grantPointsForQuestionApproved(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return;
        }
        LocalDateTime todayStart = LocalDate.now(ZONE_ID).atStartOfDay();
        long countToday = integralRecordService.count(new LambdaQueryWrapper<IntegralRecord>()
                .eq(IntegralRecord::getUserId, userId)
                .eq(IntegralRecord::getIntegralSource, INTEGRAL_SOURCE_QUESTION_APPROVED)
                .ge(IntegralRecord::getCreateTime, todayStart));
        if (countToday >= QUESTION_APPROVED_DAILY_CAP) {
            return;
        }
        User user = userService.getById(userId);
        if (user == null) {
            return;
        }
        int currentIntegral = user.getTotalIntegral() != null ? user.getTotalIntegral() : 0;
        int newBalance = currentIntegral + QUESTION_APPROVED_POINTS;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setTotalIntegral(newBalance);
        userService.updateById(updateUser);

        Question question = getById(questionId);
        String titlePart = (question != null && question.getTitle() != null)
                ? (question.getTitle().length() > 50 ? question.getTitle().substring(0, 50) + "…" : question.getTitle())
                : "";
        String relatedDesc = "问答审核通过-问答ID:" + questionId + (titlePart.isEmpty() ? "" : "-标题:" + titlePart) + "-获得" + QUESTION_APPROVED_POINTS + "积分";

        IntegralRecord record = new IntegralRecord();
        record.setUserId(userId);
        record.setIntegralType(INTEGRAL_TYPE_GET);
        record.setIntegralSource(INTEGRAL_SOURCE_QUESTION_APPROVED);
        record.setIntegralNum(QUESTION_APPROVED_POINTS);
        record.setRelatedId(questionId);
        record.setRelatedDesc(relatedDesc);
        record.setBalance(newBalance);
        record.setCreateTime(LocalDateTime.now());
        integralRecordService.save(record);
    }

}
