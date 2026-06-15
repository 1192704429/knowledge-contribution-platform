package com.kp.mall.module.notice.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.notice.entity.Notice;
import com.kp.mall.module.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 公告Controller
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询公告列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Notice entity) {
        if (entity == null) {
            entity = new Notice();
        }
        return Result.success(noticeService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有公告列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Notice entity) {
        if (entity == null) {
            entity = new Notice();
        }
        List<Notice> list = noticeService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询公告
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        return Result.success(notice);
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result save(@RequestBody Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        boolean success = noticeService.save(notice);
        if (success) {
            return Result.success("新增成功", notice);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新公告
     */
    @PutMapping
    public Result update(@RequestBody Notice notice) {
        boolean success = noticeService.updateById(notice);
        if (success) {
            return Result.success("更新成功", notice);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除公告
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = noticeService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除公告
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = noticeService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：admin
     */
    @GetMapping("/select/listAdmin")
    public Result listAdmin() {
        List<SelectVO> list = noticeService.listAdmin();
        return Result.success(list);
    }

}
