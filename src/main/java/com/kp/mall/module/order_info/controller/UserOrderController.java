package com.kp.mall.module.order_info.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.common.Result;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.module.order_info.service.OrderInfoService;
import com.kp.mall.module.order_item.entity.OrderItem;
import com.kp.mall.module.order_item.service.OrderItemService;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import com.kp.mall.module.shopping_cart.service.ShoppingCartService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 用户端订单接口：/user/order
 * 从购物车创建订单，支持积分抵扣（10积分=1元）；我的订单列表与详情
 */
@RestController
@RequestMapping("/user/order")
public class UserOrderController {

    private static final int INTEGRAL_PER_YUAN = 10;
    /** 积分类型：2-消耗 */
    private static final int INTEGRAL_TYPE_CONSUME = 2;
    /** 积分来源：5-订单抵扣 */
    private static final int INTEGRAL_SOURCE_ORDER = 5;

    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntegralRecordService integralRecordService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 从购物车创建订单
     * 请求体：{ "cartIds": [1, 2, 3], "useIntegral": 100 }
     * 10积分=1元，积分不足时只抵扣可用部分；实付金额=商品总价-积分抵扣金额
     */
    @PostMapping
    public Result create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        @SuppressWarnings("unchecked")
        List<Number> cartIdList = (List<Number>) body.get("cartIds");
        if (cartIdList == null || cartIdList.isEmpty()) {
            return Result.error("请选择要结算的购物车商品");
        }
        int useIntegral = 0;
        Object ui = body.get("useIntegral");
        if (ui != null) {
            useIntegral = ui instanceof Number ? ((Number) ui).intValue() : Integer.parseInt(ui.toString());
        }
        if (useIntegral < 0) useIntegral = 0;

        String receiverName = body.get("receiverName") != null ? body.get("receiverName").toString().trim() : "";
        String receiverPhone = body.get("receiverPhone") != null ? body.get("receiverPhone").toString().trim() : "";
        String receiverAddress = body.get("receiverAddress") != null ? body.get("receiverAddress").toString().trim() : "";
        if (StrUtil.isBlank(receiverName)) {
            return Result.error("收货人姓名不能为空");
        }
        if (StrUtil.isBlank(receiverPhone)) {
            return Result.error("收货人手机号不能为空");
        }
        if (!receiverPhone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("收货人手机号格式不正确");
        }
        if (StrUtil.isBlank(receiverAddress)) {
            return Result.error("收货地址不能为空");
        }
        if (receiverName.length() > 50) {
            return Result.error("收货人姓名不能超过50字");
        }
        if (receiverPhone.length() > 20) {
            return Result.error("收货人手机号不能超过20位");
        }
        if (receiverAddress.length() > 500) {
            return Result.error("收货地址不能超过500字");
        }

        List<ShoppingCart> cartList = new ArrayList<>();
        for (Number n : cartIdList) {
            Long id = n.longValue();
            ShoppingCart cart = shoppingCartService.getById(id);
            if (cart == null) continue;
            if (!cart.getUserId().equals(userId)) continue;
            cartList.add(cart);
        }
        if (cartList.isEmpty()) {
            return Result.error("未找到有效的购物车商品");
        }

        BigDecimal orderAmount = BigDecimal.ZERO;
        for (ShoppingCart c : cartList) {
            BigDecimal price = c.getProductPrice() != null ? c.getProductPrice() : BigDecimal.ZERO;
            int num = c.getProductNum() != null ? c.getProductNum() : 1;
            orderAmount = orderAmount.add(price.multiply(BigDecimal.valueOf(num)));
        }
        orderAmount = orderAmount.setScale(2, RoundingMode.HALF_UP);

        User user = userService.getById(userId);
        int userIntegral = (user != null && user.getTotalIntegral() != null) ? user.getTotalIntegral() : 0;
        if (useIntegral > userIntegral) useIntegral = userIntegral;

        BigDecimal integralAmount = BigDecimal.valueOf(useIntegral).divide(BigDecimal.valueOf(INTEGRAL_PER_YUAN), 2, RoundingMode.HALF_UP);
        BigDecimal payAmount = orderAmount.subtract(integralAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

        String orderCode = "ORD" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));

        OrderInfo order = new OrderInfo();
        order.setOrderCode(orderCode);
        order.setUserId(userId);
        order.setOrderAmount(orderAmount);
        order.setPayAmount(payAmount);
        order.setIntegralDeduct(useIntegral);
        order.setIntegralAmount(integralAmount);
        // 支付方式：1=纯积分(实付为0) 2=纯付款(使用积分为0) 3=积分+付款(两者都有)
        int payType;
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            payType = 1; // 纯积分
        } else if (useIntegral <= 0) {
            payType = 2; // 纯付款
        } else {
            payType = 3; // 积分+付款
        }
        order.setPayType(payType);
        order.setPayStatus(payAmount.compareTo(BigDecimal.ZERO) <= 0 ? 1 : 0); // 0未支付 1已支付
        order.setOrderStatus(payAmount.compareTo(BigDecimal.ZERO) <= 0 ? 1 : 0); // 0待付款 1待发货
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setCreateTime(LocalDateTime.now());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            order.setPayTime(LocalDateTime.now());
        }
        orderInfoService.save(order);

        Long orderId = order.getId();
        for (ShoppingCart c : cartList) {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(c.getProductId());
            item.setProductName(c.getProductName());
            item.setProductImg(c.getProductCoverImg());
            item.setProductPrice(c.getProductPrice());
            item.setProductNum(c.getProductNum());
            item.setProductIntegral(c.getProductIntegral());
            BigDecimal p = c.getProductPrice() != null ? c.getProductPrice() : BigDecimal.ZERO;
            int num = c.getProductNum() != null ? c.getProductNum() : 1;
            item.setItemAmount(p.multiply(BigDecimal.valueOf(num)).setScale(2, RoundingMode.HALF_UP));
            item.setCreateTime(LocalDateTime.now());
            orderItemService.save(item);
        }

        if (useIntegral > 0 && user != null) {
            int newBalance = userIntegral - useIntegral;
            user.setTotalIntegral(newBalance);
            userService.updateById(user);
            IntegralRecord record = new IntegralRecord();
            record.setUserId(userId);
            record.setIntegralType(INTEGRAL_TYPE_CONSUME);
            record.setIntegralSource(INTEGRAL_SOURCE_ORDER);
            record.setIntegralNum(useIntegral);
            record.setRelatedId(orderId);
            String integralYuan = integralAmount.setScale(2, RoundingMode.HALF_UP).toString();
            record.setRelatedDesc("订单抵扣-订单号:" + orderCode + "-订单ID:" + orderId + "-抵扣积分:" + useIntegral + "(折合¥" + integralYuan + "元)");
            record.setBalance(newBalance);
            record.setCreateTime(LocalDateTime.now());
            integralRecordService.save(record);
        }

        for (ShoppingCart c : cartList) {
            shoppingCartService.removeById(c.getId());
        }

        return Result.success("下单成功", order);
    }

    /**
     * 我的订单列表（分页）
     * 请求体：{ "current": 1, "size": 10, "orderStatus": null, "payStatus": null }
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Integer current = 1;
        Integer size = 10;
        if (params != null) {
            if (params.get("current") != null) current = Integer.valueOf(params.get("current").toString());
            if (params.get("size") != null) size = Integer.valueOf(params.get("size").toString());
        }
        OrderInfo entity = new OrderInfo();
        entity.setUserId(userId);
        if (params != null) {
            if (params.get("orderStatus") != null) entity.setOrderStatus(Integer.valueOf(params.get("orderStatus").toString()));
            if (params.get("payStatus") != null) entity.setPayStatus(Integer.valueOf(params.get("payStatus").toString()));
        }
        IPage<OrderInfo> page = orderInfoService.pageQuery(current, size, entity);
        return Result.success(page);
    }

    /**
     * 订单详情（仅能查看自己的订单）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        OrderInfo order = orderInfoService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            return Result.error("无权查看该订单");
        }
        return Result.success(order);
    }

    /**
     * 订单商品明细（仅能查看自己订单的明细）
     */
    @GetMapping("/{id}/items")
    public Result getOrderItems(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        OrderInfo order = orderInfoService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            return Result.error("无权查看该订单");
        }
        OrderItem query = new OrderItem();
        query.setOrderId(id);
        List<OrderItem> items = orderItemService.listQuery(query);
        return Result.success(items);
    }

    /**
     * 确认收货
     */
    @PutMapping("/{id}/receive")
    public Result receive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        OrderInfo order = orderInfoService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            return Result.error("无权操作");
        }
        order.setOrderStatus(3); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        boolean success = orderInfoService.updateById(order);
        if (success) {
            return Result.success("确认收货成功", null);
        }
        return Result.error("操作失败");
    }
}
