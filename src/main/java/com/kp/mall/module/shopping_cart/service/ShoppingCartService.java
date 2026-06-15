package com.kp.mall.module.shopping_cart.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 购物车Service接口
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 分页查询
     */
    IPage<ShoppingCart> pageQuery(Integer current, Integer size, ShoppingCart entity);

    /**
     * 列表查询（不分页）
     */
    List<ShoppingCart> listQuery(ShoppingCart entity);

    /**
     * 根据ID查询
     */
    ShoppingCart getById(Long id);

    /**
     * 添加或合并购物车（同商品累加数量）
     */
    void addOrUpdate(Long userId, Long productId, Integer quantity);

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

    /**
     * 下拉数据：product
     */
    List<SelectVO> listProduct();

}
