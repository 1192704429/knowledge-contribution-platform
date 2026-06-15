package com.kp.mall.module.user.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 用户Controller
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) User entity) {
        if (entity == null) {
            entity = new User();
        }
        return Result.success(userService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有用户列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) User entity) {
        if (entity == null) {
            entity = new User();
        }
        List<User> list = userService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result save(@RequestBody User user) {
        user.setCreateTime(LocalDateTime.now());
        boolean success = userService.save(user);
        if (success) {
            return Result.success("新增成功", user);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Result update(@RequestBody User user) {
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("更新成功", user);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除用户
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = userService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
