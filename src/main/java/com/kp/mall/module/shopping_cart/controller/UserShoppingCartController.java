package com.kp.mall.module.shopping_cart.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.common.Result;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import com.kp.mall.module.shopping_cart.service.ShoppingCartService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户端购物车接口：/user/cart
 * 所有操作均基于当前登录用户。
 */
@RestController
@RequestMapping("/user/cart")
public class UserShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 加入购物车（同商品会累加数量）
     * 请求体：{ "productId": 1, "quantity": 1 }
     */
    @PostMapping
    public Result add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Object pid = body.get("productId");
        Object qty = body.get("quantity");
        if (pid == null) {
            return Result.error("请选择商品");
        }
        long productId = Long.parseLong(pid.toString());
        int quantity = qty != null ? Integer.parseInt(qty.toString()) : 1;
        if (quantity < 1) quantity = 1;
        shoppingCartService.addOrUpdate(userId, productId, quantity);
        return Result.success("已加入购物车", null);
    }

    /**
     * 获取当前用户购物车列表（含商品信息）
     */
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        ShoppingCart query = new ShoppingCart();
        query.setUserId(userId);
        List<ShoppingCart> list = shoppingCartService.listQuery(query);
        return Result.success(list);
    }

    /**
     * 更新购物车项数量
     * 请求体：{ "id": 购物车项ID, "productNum": 数量 }
     */
    @PutMapping
    public Result update(@RequestBody ShoppingCart cart, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (cart.getId() == null) {
            return Result.error("缺少购物车项ID");
        }
        ShoppingCart existing = shoppingCartService.getById(cart.getId());
        if (existing == null) {
            return Result.error("购物车项不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        if (cart.getProductNum() != null && cart.getProductNum() >= 0) {
            existing.setProductNum(cart.getProductNum());
            shoppingCartService.updateById(existing);
        }
        return Result.success("更新成功", existing);
    }

    /**
     * 删除一条购物车项
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        ShoppingCart existing = shoppingCartService.getById(id);
        if (existing == null) {
            return Result.error("购物车项不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        shoppingCartService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除购物车项
     * 请求体：[ 1, 2, 3 ]
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.success("未选择任何项");
        }
        for (Long id : ids) {
            ShoppingCart existing = shoppingCartService.getById(id);
            if (existing != null && existing.getUserId().equals(userId)) {
                shoppingCartService.removeById(id);
            }
        }
        return Result.success("批量删除成功");
    }
}
