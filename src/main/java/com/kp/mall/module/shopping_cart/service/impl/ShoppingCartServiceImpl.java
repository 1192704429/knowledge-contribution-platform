package com.kp.mall.module.shopping_cart.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import com.kp.mall.module.shopping_cart.mapper.ShoppingCartMapper;
import com.kp.mall.module.shopping_cart.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 购物车Service实现类
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public IPage<ShoppingCart> pageQuery(Integer current, Integer size, ShoppingCart entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<ShoppingCart> records = shoppingCartMapper.pageQuery(entity, offset, limit);
        Long total = shoppingCartMapper.countQuery(entity);

        Page<ShoppingCart> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<ShoppingCart> listQuery(ShoppingCart entity) {
        return shoppingCartMapper.listQuery(entity);
    }

    @Override
    public ShoppingCart getById(Long id) {
        return shoppingCartMapper.getById(id);
    }

    @Override
    public void addOrUpdate(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null || quantity == null || quantity < 1) {
            return;
        }
        ShoppingCart existing = shoppingCartMapper.selectByUserIdAndProductId(userId, productId);
        if (existing != null) {
            existing.setProductNum((existing.getProductNum() == null ? 0 : existing.getProductNum()) + quantity);
            shoppingCartMapper.updateById(existing);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setProductNum(quantity);
            cart.setCreateTime(java.time.LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return shoppingCartMapper.listUser();
    }

    /**
     * 下拉数据：product
     */
    @Override
    public List<SelectVO> listProduct() {
        return shoppingCartMapper.listProduct();
    }

}
