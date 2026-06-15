package com.kp.mall.module.product_category.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.product_category.entity.ProductCategory;

import java.util.List;

/**
 * @author wuming
 * @description: 商品分类Service接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 分页查询
     */
    IPage<ProductCategory> pageQuery(Integer current, Integer size, ProductCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<ProductCategory> listQuery(ProductCategory entity);

    /**
     * 根据ID查询
     */
    ProductCategory getById(Long id);

}
