package com.kp.mall.module.notice.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.notice.entity.Notice;
import com.kp.mall.module.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wuming
 * @description: 用户端公告Controller
 */
@RestController
@RequestMapping("/user/notice")
public class UserNoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询公告列表（用户端，只返回状态正常的，按发布时间排序）
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        Notice entity = new Notice();
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
            if (params.get("title") != null) {
                entity.setTitle(params.get("title").toString());
            }
        }
        
        // 用户端只显示状态正常的公告
        entity.setStatus(1);
        return Result.success(noticeService.pageQueryForUser(current, size, entity));
    }

    /**
     * 根据ID查询公告详情（用户端）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        // 检查公告状态
        if (notice.getStatus() == null || notice.getStatus() != 1) {
            return Result.error("公告不存在或已删除");
        }
        return Result.success(notice);
    }
}
