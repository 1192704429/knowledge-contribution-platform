package com.kp.mall.module.shopping_cart.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import com.kp.mall.module.shopping_cart.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 购物车Controller
 */
@RestController
@RequestMapping("/shopping_cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 分页查询购物车列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) ShoppingCart entity) {
        if (entity == null) {
            entity = new ShoppingCart();
        }
        return Result.success(shoppingCartService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有购物车列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) ShoppingCart entity) {
        if (entity == null) {
            entity = new ShoppingCart();
        }
        List<ShoppingCart> list = shoppingCartService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询购物车
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ShoppingCart shoppingCart = shoppingCartService.getById(id);
        if (shoppingCart == null) {
            return Result.error("购物车不存在");
        }
        return Result.success(shoppingCart);
    }

    /**
     * 新增购物车
     */
    @PostMapping
    public Result save(@RequestBody ShoppingCart shoppingCart) {
        shoppingCart.setCreateTime(LocalDateTime.now());
        boolean success = shoppingCartService.save(shoppingCart);
        if (success) {
            return Result.success("新增成功", shoppingCart);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新购物车
     */
    @PutMapping
    public Result update(@RequestBody ShoppingCart shoppingCart) {
        boolean success = shoppingCartService.updateById(shoppingCart);
        if (success) {
            return Result.success("更新成功", shoppingCart);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除购物车
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = shoppingCartService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除购物车
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = shoppingCartService.removeByIds(ids);
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
        List<SelectVO> list = shoppingCartService.listUser();
        return Result.success(list);
    }

    /**
     * 下拉数据接口：product
     */
    @GetMapping("/select/listProduct")
    public Result listProduct() {
        List<SelectVO> list = shoppingCartService.listProduct();
        return Result.success(list);
    }

}
