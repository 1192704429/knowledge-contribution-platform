package com.kp.mall.module.qa_category.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.qa_category.entity.QaCategory;
import com.kp.mall.module.qa_category.service.QaCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 问答分类Controller
 */
@RestController
@RequestMapping("/qa_category")
public class QaCategoryController {

    @Autowired
    private QaCategoryService qaCategoryService;

    /**
     * 分页查询问答分类列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) QaCategory entity) {
        if (entity == null) {
            entity = new QaCategory();
        }
        return Result.success(qaCategoryService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有问答分类列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) QaCategory entity) {
        if (entity == null) {
            entity = new QaCategory();
        }
        List<QaCategory> list = qaCategoryService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询问答分类
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        QaCategory qaCategory = qaCategoryService.getById(id);
        if (qaCategory == null) {
            return Result.error("问答分类不存在");
        }
        return Result.success(qaCategory);
    }

    /**
     * 新增问答分类
     */
    @PostMapping
    public Result save(@RequestBody QaCategory qaCategory) {
        qaCategory.setCreateTime(LocalDateTime.now());
        boolean success = qaCategoryService.save(qaCategory);
        if (success) {
            return Result.success("新增成功", qaCategory);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新问答分类
     */
    @PutMapping
    public Result update(@RequestBody QaCategory qaCategory) {
        boolean success = qaCategoryService.updateById(qaCategory);
        if (success) {
            return Result.success("更新成功", qaCategory);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除问答分类
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = qaCategoryService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除问答分类
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = qaCategoryService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
