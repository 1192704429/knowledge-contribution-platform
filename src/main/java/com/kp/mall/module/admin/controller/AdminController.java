package com.kp.mall.module.admin.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.admin.entity.Admin;
import com.kp.mall.module.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 管理员Controller
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询管理员列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Admin entity) {
        if (entity == null) {
            entity = new Admin();
        }
        return Result.success(adminService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有管理员列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Admin entity) {
        if (entity == null) {
            entity = new Admin();
        }
        List<Admin> list = adminService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询管理员
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        return Result.success(admin);
    }

    /**
     * 新增管理员
     */
    @PostMapping
    public Result save(@RequestBody Admin admin) {
        admin.setCreateTime(LocalDateTime.now());
        boolean success = adminService.save(admin);
        if (success) {
            return Result.success("新增成功", admin);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新管理员
     */
    @PutMapping
    public Result update(@RequestBody Admin admin) {
        boolean success = adminService.updateById(admin);
        if (success) {
            return Result.success("更新成功", admin);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除管理员
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = adminService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除管理员
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = adminService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
