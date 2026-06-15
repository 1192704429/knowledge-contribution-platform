package com.kp.mall.basic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.module.order_info.service.OrderInfoService;
import com.kp.mall.module.order_item.entity.OrderItem;
import com.kp.mall.module.order_item.service.OrderItemService;
import com.kp.mall.module.product.entity.Product;
import com.kp.mall.module.product.service.ProductService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝异步通知回调（由支付宝服务器调用，不经过前端）
 * 配置 alipay.notifyUrl 指向本类 notify 接口的完整外网地址。
 * 支付成功回调逻辑：订单改为已支付；若有积分抵扣则扣减用户积分并写记录；扣减商品库存；增加商品销量。
 */
@RestController
@RequestMapping("/alipay")
@Slf4j
public class AliPayController {

    /** 积分来源：5-订单抵扣 */
    private static final int INTEGRAL_SOURCE_ORDER = 5;
    /** 积分类型：2-消耗 */
    private static final int INTEGRAL_TYPE_CONSUME = 2;

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntegralRecordService integralRecordService;
    @Autowired
    private ProductService productService;

    /**
     * 支付宝支付结果异步通知
     * 支付成功后支付宝会 POST 请求此地址，需配置为外网可访问的 URL
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST, RequestMethod.GET})
    public String payNotify(HttpServletRequest request) {
        String method = request.getMethod();
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            params.put(name, values != null && values.length > 0 ? values[0] : "");
        }
        log.info("[payNotify] 收到支付宝回调, method={}, params={}", method, params);

        // 浏览器探活或手工访问通常是 GET，不参与支付状态处理
        if (!"POST".equalsIgnoreCase(method)) {
            log.info("[payNotify] 非POST请求，直接返回success, method={}", method);
            return "success";
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.info("[payNotify] 非成功交易状态, tradeStatus={}, outTradeNo={}", tradeStatus, params.get("out_trade_no"));
            return "success"; // 非成功也返回 success，避免支付宝重复通知
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        if (outTradeNo == null || outTradeNo.isEmpty()) {
            log.warn("[payNotify] 回调缺少 out_trade_no, tradeNo={}", tradeNo);
            return "success";
        }
        log.info("[payNotify] 开始处理支付成功回调, outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);

        // 先按业务订单号查；若 out_trade_no 为纯数字则再按主键ID兜底
        OrderInfo order = orderInfoService.getOne(
            new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderCode, outTradeNo)
                .last("LIMIT 1")
        );
        if (order == null && outTradeNo.matches("\\d+")) {
            order = orderInfoService.getById(Long.parseLong(outTradeNo));
            if (order != null) {
                log.info("[payNotify] 按订单ID兜底命中, outTradeNo={}, orderId={}, orderCode={}",
                    outTradeNo, order.getId(), order.getOrderCode());
            }
        }
        if (order == null) {
            log.warn("[payNotify] 未找到对应订单, outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);
            return "success";
        }
        // 已处理过则直接返回，避免重复扣库存/积分
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            log.info("[payNotify] 订单已处理过, orderId={}, orderCode={}, payOrderNo={}",
                order.getId(), order.getOrderCode(), order.getPayOrderNo());
            return "success";
        }

        // 1. 订单状态改为已支付
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayOrderNo(tradeNo);
        order.setOrderStatus(1); // 待发货
        orderInfoService.updateById(order);
        log.info("[payNotify] 订单状态更新成功, orderId={}, orderCode={}, payOrderNo={}",
            order.getId(), order.getOrderCode(), tradeNo);

        Long orderId = order.getId();
        Long userId = order.getUserId();

        // 2. 若有积分抵扣，扣减用户积分并写入消耗记录（若创建订单时已扣过则不再扣）
        Integer integralDeduct = order.getIntegralDeduct();
        if (integralDeduct != null && integralDeduct > 0 && userId != null) {
            long existCount = integralRecordService.count(
                new LambdaQueryWrapper<IntegralRecord>()
                    .eq(IntegralRecord::getRelatedId, orderId)
                    .eq(IntegralRecord::getIntegralSource, INTEGRAL_SOURCE_ORDER)
            );
            if (existCount == 0) {
                User user = userService.getById(userId);
                if (user != null) {
                    int current = user.getTotalIntegral() != null ? user.getTotalIntegral() : 0;
                    int newBalance = Math.max(0, current - integralDeduct);
                    user.setTotalIntegral(newBalance);
                    userService.updateById(user);
                    IntegralRecord record = new IntegralRecord();
                    record.setUserId(userId);
                    record.setIntegralType(INTEGRAL_TYPE_CONSUME);
                    record.setIntegralSource(INTEGRAL_SOURCE_ORDER);
                    record.setIntegralNum(integralDeduct);
                    record.setRelatedId(orderId);
                    BigDecimal integralYuan = BigDecimal.valueOf(integralDeduct).divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
                    record.setRelatedDesc("订单支付抵扣-订单号:" + order.getOrderCode() + "-抵扣积分:" + integralDeduct + "(折合¥" + integralYuan + "元)");
                    record.setBalance(newBalance);
                    record.setCreateTime(LocalDateTime.now());
                    integralRecordService.save(record);
                    log.info("[payNotify] 积分扣减成功, userId={}, orderId={}, deduct={}, balance={}",
                        userId, orderId, integralDeduct, newBalance);
                }
            } else {
                log.info("[payNotify] 积分记录已存在，跳过重复扣减, orderId={}, integralDeduct={}", orderId, integralDeduct);
            }
        }

        // 3. 扣减商品库存并增加销量
        OrderItem queryItem = new OrderItem();
        queryItem.setOrderId(orderId);
        List<OrderItem> items = orderItemService.listQuery(queryItem);
        for (OrderItem item : items) {
            if (item.getProductId() == null) continue;
            Product product = productService.getById(item.getProductId());
            if (product == null) continue;
            int num = item.getProductNum() != null ? item.getProductNum() : 0;
            if (num <= 0) continue;
            // 扣减库存（不低于 0）
            int stock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(Math.max(0, stock - num));
            // 增加销量
            int sales = product.getSalesCount() != null ? product.getSalesCount() : 0;
            product.setSalesCount(sales + num);
            productService.updateById(product);
            log.info("[payNotify] 商品库存/销量更新, orderId={}, productId={}, buyNum={}, stock:{}->{}, sales:{}->{}",
                orderId, item.getProductId(), num, stock, product.getStock(), sales, product.getSalesCount());
        }

        log.info("[payNotify] 回调处理完成, orderId={}, orderCode={}", orderId, order.getOrderCode());
        return "success";
    }
}
