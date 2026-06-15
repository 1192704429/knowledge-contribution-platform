package com.kp.mall.module.order_info.controller;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayApiException;
import com.kp.mall.common.Result;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.module.order_info.service.OrderInfoService;
import com.kp.mall.util.AlipayUtil;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户端支付接口：/user/pay
 * 拉起支付宝时返回表单 HTML，前端提交该表单即可跳转支付宝收银台。
 */
@RestController
@RequestMapping("/user/pay")
public class UserPayController {

    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private AlipayUtil alipayUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 获取支付宝支付表单
     * 请求体：{ "orderId": 订单主键 id }
     * 返回：{ "code": 200, "data": "表单 HTML 字符串" }，前端将 data 插入页面并 submit 表单即可调起支付宝。
     */
    @PostMapping("/alipay")
    public Result alipay(@RequestBody Map<String, Object> body, HttpServletRequest request) throws AlipayApiException {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Object oid = body.get("orderId");
        if (oid == null) {
            return Result.error("缺少订单ID");
        }
        Long orderId = oid instanceof Number ? ((Number) oid).longValue() : Long.parseLong(oid.toString());
        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作该订单");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return Result.error("订单已支付");
        }
        BigDecimal payAmount = order.getPayAmount();
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("订单无需支付");
        }
        String orderCode = order.getOrderCode() != null ? order.getOrderCode() : ("ORD" + orderId);
        String subject = "知识贡献平台-订单" + orderCode;
        String formHtml = alipayUtil.pay(orderCode, payAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString(), subject);
        return Result.success(formHtml);
    }

    /**
     * 模拟支付宝回调成功（用于本地/联调兜底）
     * 请求体：{ "orderId": 订单主键 id }
     */
    @PostMapping("/mock-notify-success")
    public Result mockNotifySuccess(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Object oid = body.get("orderId");
        if (oid == null) {
            return Result.error("缺少订单ID");
        }
        Long orderId = oid instanceof Number ? ((Number) oid).longValue() : Long.parseLong(oid.toString());
        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作该订单");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return Result.success("订单已是支付状态");
        }
        order.setPayStatus(1);
        order.setOrderStatus(1); // 待发货
        order.setPayTime(LocalDateTime.now());
        order.setPayOrderNo("MOCK-" + order.getId());
        orderInfoService.updateById(order);
        return Result.success("模拟支付成功，订单状态已更新");
    }
}
