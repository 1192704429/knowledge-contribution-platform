package com.kp.mall.module.answer.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.answer.entity.Answer;
import com.kp.mall.module.answer.mapper.AnswerMapper;
import com.kp.mall.module.answer.service.AnswerService;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.question.entity.Question;
import com.kp.mall.module.question.service.QuestionService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import com.kp.mall.util.AiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuming
 * @description: 问答回答Service实现类
 */
@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerServiceImpl.class);
    private static final int INTEGRAL_TYPE_GET = 1;
    /** 积分来源：3-问答优质回答(20分) */
    private static final int INTEGRAL_SOURCE_ANSWER_EXCELLENT = 3;
    /** 积分来源：4-问答良好回答(15分，每日5次) */
    private static final int INTEGRAL_SOURCE_ANSWER_GOOD = 4;
    /** 未评级（AI 未返回或解析失败） */
    private static final int GRADE_UNRATED = 0;
    private static final int GRADE_GOOD = 1;
    private static final int GRADE_EXCELLENT = 2;
    /** AI 返回「其他」时的评级 */
    private static final int GRADE_OTHER = 3;
    private static final int POINTS_EXCELLENT = 20;
    private static final int POINTS_GOOD = 15;
    private static final int GOOD_DAILY_CAP = 5;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String AI_SYSTEM = "你是一个问答质量评判助手。根据题目标题和用户回答内容，仅回复以下三个词之一，不要任何解释或标点：优秀、良好、其他。";

    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private AiChatClient aiChatClient;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private IntegralRecordService integralRecordService;
    @Autowired
    private UserService userService;

    @Override
    public IPage<Answer> pageQuery(Integer current, Integer size, Answer entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Answer> records = answerMapper.pageQuery(entity, offset, limit);
        Long total = answerMapper.countQuery(entity);

        Page<Answer> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Answer> listQuery(Answer entity) {
        return answerMapper.listQuery(entity);
    }

    @Override
    public Answer getById(Long id) {
        return answerMapper.getById(id);
    }

    /**
     * 下拉数据：question
     */
    @Override
    public List<SelectVO> listQuestion() {
        return answerMapper.listQuestion();
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return answerMapper.listUser();
    }

    /**
     * 下拉数据：admin
     */
    @Override
    public List<SelectVO> listAdmin() {
        return answerMapper.listAdmin();
    }

    @Override
    public void approveAndGrade(Long answerId) {
        doApproveAndGrade(answerId);
    }

    @Override
    public void approveAndGradeAsync(Long answerId) {
        CompletableFuture.runAsync(() -> {
            try {
                doApproveAndGrade(answerId);
            } catch (Exception e) {
                log.error("回答AI评级异步执行失败, answerId={}", answerId, e);
            }
        });
    }

    /**
     * 内部方法：调用 AI 评判质量，更新 answerGrade（已评级），并发放积分。
     */
    private void doApproveAndGrade(Long answerId) {
        if (answerId == null) return;
        Answer answer = answerMapper.getById(answerId);
        if (answer == null || answer.getAuditStatus() == null || answer.getAuditStatus() != 1) return;
        Question question = questionService.getById(answer.getQuestionId());
        if (question == null) return;
        // 防刷分兜底：提问人自己回答自己的问题，不参与问答积分奖励
        if (question.getUserId() != null && question.getUserId().equals(answer.getUserId())) {
            log.info("自问自答不发积分, questionId={}, answerId={}, userId={}",
                    question.getId(), answerId, answer.getUserId());
            return;
        }
        String questionTitle = StrUtil.nullToEmpty(question.getTitle()).trim();
        String answerContent = StrUtil.nullToEmpty(answer.getContent()).replaceAll("<[^>]*>", "").trim();
        if (answerContent.length() > 2000) answerContent = answerContent.substring(0, 2000);
        String userMsg = "题目：" + questionTitle + "\n回答：" + answerContent;
        String aiResult = aiChatClient.chat(AI_SYSTEM, userMsg);
        int grade = parseGrade(aiResult);
        answer.setAnswerGrade(grade);
        this.updateById(answer);

        if (grade == GRADE_EXCELLENT) {
            grantPoints(answer.getUserId(), answerId, INTEGRAL_SOURCE_ANSWER_EXCELLENT, POINTS_EXCELLENT, "问答优质回答-回答ID:" + answerId + "-获得" + POINTS_EXCELLENT + "积分");
        } else if (grade == GRADE_GOOD) {
            LocalDateTime todayStart = LocalDate.now(ZONE_ID).atStartOfDay();
            long countToday = integralRecordService.count(new LambdaQueryWrapper<IntegralRecord>()
                    .eq(IntegralRecord::getUserId, answer.getUserId())
                    .eq(IntegralRecord::getIntegralSource, INTEGRAL_SOURCE_ANSWER_GOOD)
                    .ge(IntegralRecord::getCreateTime, todayStart));
            if (countToday < GOOD_DAILY_CAP) {
                grantPoints(answer.getUserId(), answerId, INTEGRAL_SOURCE_ANSWER_GOOD, POINTS_GOOD, "问答良好回答-回答ID:" + answerId + "-获得" + POINTS_GOOD + "积分");
            }
        }
    }

    private int parseGrade(String aiResult) {
        if (StrUtil.isBlank(aiResult)) return GRADE_UNRATED;
        String s = aiResult.trim();
        if (s.contains("优秀")) return GRADE_EXCELLENT;
        if (s.contains("良好")) return GRADE_GOOD;
        if (s.contains("其他")) return GRADE_OTHER;
        return GRADE_UNRATED;
    }

    private void grantPoints(Long userId, Long answerId, int source, int points, String desc) {
        if (userId == null) return;
        User user = userService.getById(userId);
        if (user == null) return;
        int current = user.getTotalIntegral() != null ? user.getTotalIntegral() : 0;
        int newBalance = current + points;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setTotalIntegral(newBalance);
        userService.updateById(updateUser);
        IntegralRecord record = new IntegralRecord();
        record.setUserId(userId);
        record.setIntegralType(INTEGRAL_TYPE_GET);
        record.setIntegralSource(source);
        record.setIntegralNum(points);
        record.setRelatedId(answerId);
        record.setRelatedDesc(desc);
        record.setBalance(newBalance);
        record.setCreateTime(LocalDateTime.now());
        integralRecordService.save(record);
    }

}
