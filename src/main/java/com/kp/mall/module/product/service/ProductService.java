package com.kp.mall.module.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.product.entity.Product;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 商品Service接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询
     */
    IPage<Product> pageQuery(Integer current, Integer size, Product entity);

    /**
     * 列表查询（不分页）
     */
    List<Product> listQuery(Product entity);

    /**
     * 根据ID查询
     */
    Product getById(Long id);

    /**
     * 下拉数据：product_category
     */
    List<SelectVO> listProductCategory();

}
