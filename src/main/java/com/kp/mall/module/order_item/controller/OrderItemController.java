package com.kp.mall.module.order_item.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.order_item.entity.OrderItem;
import com.kp.mall.module.order_item.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 订单明细Controller
 */
@RestController
@RequestMapping("/order_item")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 分页查询订单明细列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) OrderItem entity) {
        if (entity == null) {
            entity = new OrderItem();
        }
        return Result.success(orderItemService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有订单明细列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) OrderItem entity) {
        if (entity == null) {
            entity = new OrderItem();
        }
        List<OrderItem> list = orderItemService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询订单明细
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        OrderItem orderItem = orderItemService.getById(id);
        if (orderItem == null) {
            return Result.error("订单明细不存在");
        }
        return Result.success(orderItem);
    }

    /**
     * 新增订单明细
     */
    @PostMapping
    public Result save(@RequestBody OrderItem orderItem) {
        orderItem.setCreateTime(LocalDateTime.now());
        boolean success = orderItemService.save(orderItem);
        if (success) {
            return Result.success("新增成功", orderItem);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新订单明细
     */
    @PutMapping
    public Result update(@RequestBody OrderItem orderItem) {
        boolean success = orderItemService.updateById(orderItem);
        if (success) {
            return Result.success("更新成功", orderItem);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除订单明细
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = orderItemService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除订单明细
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = orderItemService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：order_info
     */
    @GetMapping("/select/listOrderInfo")
    public Result listOrderInfo() {
        List<SelectVO> list = orderItemService.listOrderInfo();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：product
     */
    @GetMapping("/select/listProduct")
    public Result listProduct() {
        List<SelectVO> list = orderItemService.listProduct();
        return Result.success(list);
    }

}
