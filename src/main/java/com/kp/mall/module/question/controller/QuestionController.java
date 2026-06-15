package com.kp.mall.module.question.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.question.entity.Question;
import com.kp.mall.module.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 问答Controller
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询问答列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Question entity) {
        if (entity == null) {
            entity = new Question();
        }
        return Result.success(questionService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有问答列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Question entity) {
        if (entity == null) {
            entity = new Question();
        }
        List<Question> list = questionService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询问答
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("问答不存在");
        }
        return Result.success(question);
    }

    /**
     * 新增问答
     */
    @PostMapping
    public Result save(@RequestBody Question question) {
        question.setCreateTime(LocalDateTime.now());
        boolean success = questionService.save(question);
        if (success) {
            return Result.success("新增成功", question);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新问答（管理员审核时若将 auditStatus 改为 1，则发放积分：每次10分，每日上限3次）
     */
    @PutMapping
    public Result update(@RequestBody Question question) {
        if (question.getId() == null) {
            return Result.error("问答ID不能为空");
        }
        Question exist = questionService.getById(question.getId());
        if (exist == null) {
            return Result.error("问答不存在");
        }
        Integer oldAuditStatus = exist.getAuditStatus();
        boolean success = questionService.updateById(question);
        if (success) {
            Integer newAuditStatus = question.getAuditStatus();
            if (newAuditStatus != null && newAuditStatus == 1 && !Integer.valueOf(1).equals(oldAuditStatus)) {
                questionService.grantPointsForQuestionApproved(exist.getUserId(), question.getId());
            }
            return Result.success("更新成功", question);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除问答
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = questionService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除问答
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = questionService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：user
     */
    @GetMapping("/select/listUser")
    public Result listUser() {
        List<SelectVO> list = questionService.listUser();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：qa_category
     */
    @GetMapping("/select/listQaCategory")
    public Result listQaCategory() {
        List<SelectVO> list = questionService.listQaCategory();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：admin
     */
    @GetMapping("/select/listAdmin")
    public Result listAdmin() {
        List<SelectVO> list = questionService.listAdmin();
        return Result.success(list);
    }

}
