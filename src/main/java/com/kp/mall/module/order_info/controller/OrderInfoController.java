package com.kp.mall.module.order_info.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.module.order_info.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 订单Controller
 */
@RestController
@RequestMapping("/order_info")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 分页查询订单列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) OrderInfo entity) {
        if (entity == null) {
            entity = new OrderInfo();
        }
        return Result.success(orderInfoService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有订单列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) OrderInfo entity) {
        if (entity == null) {
            entity = new OrderInfo();
        }
        List<OrderInfo> list = orderInfoService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        OrderInfo orderInfo = orderInfoService.getById(id);
        if (orderInfo == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orderInfo);
    }

    /**
     * 新增订单
     */
    @PostMapping
    public Result save(@RequestBody OrderInfo orderInfo) {
        orderInfo.setCreateTime(LocalDateTime.now());
        boolean success = orderInfoService.save(orderInfo);
        if (success) {
            return Result.success("新增成功", orderInfo);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新订单
     */
    @PutMapping
    public Result update(@RequestBody OrderInfo orderInfo) {
        boolean success = orderInfoService.updateById(orderInfo);
        if (success) {
            return Result.success("更新成功", orderInfo);
        }
        return Result.error("更新失败");
    }

    /**
     * 发货：仅待发货(1)可操作，填写物流单号（可选，虚拟物品可不填），自动填充发货时间，订单状态改为待收货(2)
     */
    @PutMapping("/{id}/deliver")
    public Result deliver(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        OrderInfo order = orderInfoService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 1) {
            return Result.error("仅待发货订单可执行发货操作");
        }
        String deliveryNo = null;
        if (body != null && body.get("deliveryNo") != null) {
            deliveryNo = body.get("deliveryNo").toString().trim();
            if (deliveryNo.isEmpty()) deliveryNo = null;
        }
        order.setDeliveryNo(deliveryNo);
        order.setDeliveryTime(LocalDateTime.now());
        order.setOrderStatus(2); // 待收货
        boolean success = orderInfoService.updateById(order);
        if (success) {
            return Result.success("发货成功", order);
        }
        return Result.error("发货失败");
    }

    /**
     * 根据ID删除订单
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = orderInfoService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除订单
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = orderInfoService.removeByIds(ids);
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
        List<SelectVO> list = orderInfoService.listUser();
        return Result.success(list);
    }

}
