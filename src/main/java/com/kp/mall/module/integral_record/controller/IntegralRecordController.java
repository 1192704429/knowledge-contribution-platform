package com.kp.mall.module.integral_record.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 积分记录Controller
 */
@RestController
@RequestMapping("/integral_record")
public class IntegralRecordController {

    @Autowired
    private IntegralRecordService integralRecordService;

    /**
     * 分页查询积分记录列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) IntegralRecord entity) {
        if (entity == null) {
            entity = new IntegralRecord();
        }
        return Result.success(integralRecordService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有积分记录列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) IntegralRecord entity) {
        if (entity == null) {
            entity = new IntegralRecord();
        }
        List<IntegralRecord> list = integralRecordService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询积分记录
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        IntegralRecord integralRecord = integralRecordService.getById(id);
        if (integralRecord == null) {
            return Result.error("积分记录不存在");
        }
        return Result.success(integralRecord);
    }

    /**
     * 新增积分记录
     */
    @PostMapping
    public Result save(@RequestBody IntegralRecord integralRecord) {
        integralRecord.setCreateTime(LocalDateTime.now());
        boolean success = integralRecordService.save(integralRecord);
        if (success) {
            return Result.success("新增成功", integralRecord);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新积分记录
     */
    @PutMapping
    public Result update(@RequestBody IntegralRecord integralRecord) {
        boolean success = integralRecordService.updateById(integralRecord);
        if (success) {
            return Result.success("更新成功", integralRecord);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除积分记录
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = integralRecordService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除积分记录
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = integralRecordService.removeByIds(ids);
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
        List<SelectVO> list = integralRecordService.listUser();
        return Result.success(list);
    }

}
