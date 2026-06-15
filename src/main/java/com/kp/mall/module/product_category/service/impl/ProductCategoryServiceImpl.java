package com.kp.mall.module.product_category.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.product_category.entity.ProductCategory;
import com.kp.mall.module.product_category.mapper.ProductCategoryMapper;
import com.kp.mall.module.product_category.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 商品分类Service实现类
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public IPage<ProductCategory> pageQuery(Integer current, Integer size, ProductCategory entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<ProductCategory> records = productCategoryMapper.pageQuery(entity, offset, limit);
        Long total = productCategoryMapper.countQuery(entity);

        Page<ProductCategory> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<ProductCategory> listQuery(ProductCategory entity) {
        return productCategoryMapper.listQuery(entity);
    }

    @Override
    public ProductCategory getById(Long id) {
        return productCategoryMapper.getById(id);
    }

}
