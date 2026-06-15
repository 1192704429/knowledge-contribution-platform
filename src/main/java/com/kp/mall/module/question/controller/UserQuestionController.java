package com.kp.mall.module.question.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.common.Result;
import com.kp.mall.module.question.entity.Question;
import com.kp.mall.module.question.service.QuestionService;
import com.kp.mall.module.qa_category.entity.QaCategory;
import com.kp.mall.module.qa_category.service.QaCategoryService;
import com.kp.mall.module.answer.entity.Answer;
import com.kp.mall.module.answer.service.AnswerService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuming
 * @description: 用户端问答Controller
 */
@RestController
@RequestMapping("/user/qa")
public class UserQuestionController {

    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private QaCategoryService qaCategoryService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;

    /**
     * 问答列表页：分页查询问答（用户端）
     * <p>
     * 请求体示例：{ "current": 1, "size": 10, "categoryId": 1, "title": "关键词" }
     * - current: 页码，默认 1
     * - size: 每页条数，默认 10
     * - categoryId: 可选，按分类筛选
     * - title: 可选，标题模糊搜索
     * </p>
     * 只返回状态正常且审核通过的问答，按发布时间倒序。
     * 响应 data 为 IPage：records、total、size、current。
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        Question entity = new Question();
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
        
        // 用户端只显示状态正常且审核通过的问答
        entity.setStatus(1);
        entity.setAuditStatus(1);
        return Result.success(questionService.pageQueryForUser(current, size, entity));
    }

    /**
     * 我的问答列表（个人中心）：分页查询当前用户发布的问答（含待审核、驳回等）
     * 请求体：{ "current": 1, "size": 10, "title": "", "auditStatus": null }
     */
    @PostMapping("/my")
    public Result myList(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Question entity = new Question();
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
        return Result.success(questionService.pageQuery(current, size, entity));
    }

    /**
     * 我的回答列表（个人中心）：分页查询当前用户发布的回答
     * 请求体：{ "current": 1, "size": 10, "title": "", "auditStatus": null, "answerGrade": null }
     */
    @PostMapping("/my-answers")
    public Result myAnswerList(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Answer entity = new Answer();
        entity.setUserId(userId);
        Integer current = 1;
        Integer size = 10;
        if (params != null) {
            if (params.get("current") != null) current = Integer.valueOf(params.get("current").toString());
            if (params.get("size") != null) size = Integer.valueOf(params.get("size").toString());
            if (params.get("title") != null && !params.get("title").toString().trim().isEmpty()) {
                entity.setTitle(params.get("title").toString().trim());
            }
            if (params.get("auditStatus") != null && !"".equals(params.get("auditStatus").toString())) {
                entity.setAuditStatus(Integer.valueOf(params.get("auditStatus").toString()));
            }
            if (params.get("answerGrade") != null && !"".equals(params.get("answerGrade").toString())) {
                entity.setAnswerGrade(Integer.valueOf(params.get("answerGrade").toString()));
            }
        }
        IPage<Answer> page = answerService.pageQuery(current, size, entity);
        return Result.success(page);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser u = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return u != null ? u.getId() : null;
    }

    /**
     * 根据ID查询问答详情（用户端）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("问答不存在");
        }
        // 检查问答状态和审核状态
        if (question.getStatus() == null || question.getStatus() != 1) {
            return Result.error("问答不存在或已删除");
        }
        if (question.getAuditStatus() == null || question.getAuditStatus() != 1) {
            return Result.error("问答未通过审核");
        }
        // 进入详情页增加浏览量
        int newViewCount = (question.getViewCount() == null ? 0 : question.getViewCount()) + 1;
        Question updateView = new Question();
        updateView.setId(question.getId());
        updateView.setViewCount(newViewCount);
        questionService.updateById(updateView);
        question.setViewCount(newViewCount);
        return Result.success(question);
    }

    /**
     * 问答详情页：获取某问答下的回复列表（用户端）
     * <p>
     * 只返回状态正常且审核通过的回复，按创建时间正序（楼层顺序）。响应 data 为 List&lt;Answer&gt;（含 userName、avatar、title 等）。
     * </p>
     */
    @GetMapping("/{questionId}/answers")
    public Result listAnswers(@PathVariable Long questionId) {
        Answer entity = new Answer();
        entity.setQuestionId(questionId);
        entity.setStatus(1);
        entity.setAuditStatus(1);
        List<Answer> list = answerService.listQuery(entity).stream()
                .sorted(Comparator.comparing(Answer::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 用户发表回复（用户端）
     * <p>
     * 请求体：{ "questionId": 1, "content": "回复内容" }。需要登录。
     * </p>
     */
    @PostMapping("/reply")
    public Result reply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录状态已过期~");
        }
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        Object qId = body.get("questionId");
        Object contentObj = body.get("content");
        if (qId == null) {
            return Result.error("问答ID不能为空");
        }
        String content = contentObj == null ? null : contentObj.toString();
        if (StrUtil.isBlank(content)) {
            return Result.error("回复内容不能为空");
        }
        Long questionId = Long.valueOf(qId.toString());
        Question question = questionService.getById(questionId);
        if (question == null || question.getStatus() == null || question.getStatus() != 1
                || question.getAuditStatus() == null || question.getAuditStatus() != 1) {
            return Result.error("问答不存在或未通过审核");
        }
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setContent(content.trim());
        answer.setUserId(loginUser.getId());
        answer.setAnswerGrade(0);   // 审核通过后由 AI 评判后设置
        answer.setAuditStatus(0);    // 待审核，审核通过后才展示并参与积分
        answer.setStatus(1);
        answer.setCreateTime(LocalDateTime.now());
        boolean success = answerService.save(answer);
        if (success) {
            return Result.success("提交成功，等待审核通过后展示", answer);
        }
        return Result.error("发表失败");
    }

    /**
     * 用户发布问答（提交审核）
     * <p>
     * 请求体：{ "title": "标题", "content": "内容", "categoryId": 1 }。需要登录。
     * 新增的问答为待审核状态（auditStatus=0），审核通过后才在列表/详情中显示。
     * </p>
     */
    @PostMapping
    public Result publish(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录状态已过期~");
        }
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        Object titleObj = body.get("title");
        Object contentObj = body.get("content");
        Object categoryIdObj = body.get("categoryId");
        if (titleObj == null || StrUtil.isBlank(titleObj.toString())) {
            return Result.error("问答标题不能为空");
        }
        if (contentObj == null || StrUtil.isBlank(contentObj.toString())) {
            return Result.error("问答内容不能为空");
        }
        if (categoryIdObj == null) {
            return Result.error("请选择问答分类");
        }
        Question question = new Question();
        question.setTitle(titleObj.toString().trim());
        question.setContent(contentObj.toString().trim());
        question.setCategoryId(Long.valueOf(categoryIdObj.toString()));
        question.setUserId(loginUser.getId());
        question.setViewCount(0);
        question.setAnswerCount(0);
        question.setAuditStatus(0); // 待审核
        question.setStatus(1);       // 正常
        question.setPublishTime(LocalDateTime.now());
        question.setCreateTime(LocalDateTime.now());
        boolean success = questionService.save(question);
        if (success) {
            return Result.success("提交成功，等待审核", question);
        }
        return Result.error("提交失败");
    }

    /**
     * 问答列表页：获取问答分类列表（用户端）
     * <p>
     * 用于左侧分类栏，只返回状态正常的分类。响应 data 为 List&lt;QaCategory&gt;。
     * </p>
     */
    @GetMapping("/category/list")
    public Result listCategory() {
        QaCategory category = new QaCategory();
        category.setStatus(1); // 只查询状态正常的分类
        List<QaCategory> list = qaCategoryService.listQuery(category);
        return Result.success(list);
    }
}
