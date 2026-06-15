package com.kp.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.product.entity.Product;
import com.kp.mall.module.product.mapper.ProductMapper;
import com.kp.mall.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 商品Service实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public IPage<Product> pageQuery(Integer current, Integer size, Product entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Product> records = productMapper.pageQuery(entity, offset, limit);
        Long total = productMapper.countQuery(entity);

        Page<Product> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Product> listQuery(Product entity) {
        return productMapper.listQuery(entity);
    }

    @Override
    public Product getById(Long id) {
        return productMapper.getById(id);
    }

    /**
     * 下拉数据：product_category
     */
    @Override
    public List<SelectVO> listProductCategory() {
        return productMapper.listProductCategory();
    }

}
