package com.kp.mall.module.answer.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.answer.entity.Answer;
import com.kp.mall.module.answer.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 问答回答Controller
 */
@RestController
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    /**
     * 分页查询问答回答列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Answer entity) {
        if (entity == null) {
            entity = new Answer();
        }
        return Result.success(answerService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有问答回答列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Answer entity) {
        if (entity == null) {
            entity = new Answer();
        }
        List<Answer> list = answerService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询问答回答
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Answer answer = answerService.getById(id);
        if (answer == null) {
            return Result.error("问答回答不存在");
        }
        return Result.success(answer);
    }

    /**
     * 新增问答回答
     */
    @PostMapping
    public Result save(@RequestBody Answer answer) {
        answer.setCreateTime(LocalDateTime.now());
        boolean success = answerService.save(answer);
        if (success) {
            return Result.success("新增成功", answer);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新问答回答（管理员修改/审核）
     * 当审核状态从待审核改为通过时，会调用 AI 评判质量并发放积分（优秀20分/良好15分每日5次）
     */
    @PutMapping
    public Result update(@RequestBody Answer answer) {
        if (answer == null || answer.getId() == null) {
            return Result.error("参数不完整");
        }
        Answer existing = answerService.getById(answer.getId());
        if (existing == null) {
            return Result.error("问答回答不存在");
        }
        boolean wasPending = Objects.equals(existing.getAuditStatus(), 0);
        if (answer.getAuditStatus() != null) {
            existing.setAuditStatus(answer.getAuditStatus());
        }
        if (answer.getAnswerGrade() != null) {
            existing.setAnswerGrade(answer.getAnswerGrade());
        }
        if (answer.getAuditRemark() != null) {
            existing.setAuditRemark(answer.getAuditRemark());
        }
        if (answer.getAuditAdminId() != null) {
            existing.setAuditAdminId(answer.getAuditAdminId());
        }
        if (answer.getAuditStatus() != null && answer.getAuditStatus() == 1) {
            existing.setAuditTime(LocalDateTime.now());
        }
        boolean success = answerService.updateById(existing);
        if (success && wasPending && existing.getAuditStatus() != null && existing.getAuditStatus() == 1) {
            answerService.approveAndGradeAsync(existing.getId());
        }
        if (success) {
            return Result.success("更新成功", answerService.getById(existing.getId()));
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除问答回答
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = answerService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除问答回答
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = answerService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：question
     */
    @GetMapping("/select/listQuestion")
    public Result listQuestion() {
        List<SelectVO> list = answerService.listQuestion();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：user
     */
    @GetMapping("/select/listUser")
    public Result listUser() {
        List<SelectVO> list = answerService.listUser();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：admin
     */
    @GetMapping("/select/listAdmin")
    public Result listAdmin() {
        List<SelectVO> list = answerService.listAdmin();
        return Result.success(list);
    }

}
