package com.kp.mall.module.product_category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.product_category.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 商品分类Mapper接口
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 分页查询
     */
    List<ProductCategory> pageQuery(@Param("entity") ProductCategory entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") ProductCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<ProductCategory> listQuery(@Param("entity") ProductCategory entity);

    /**
     * 根据ID查询
     */
    ProductCategory getById(@Param("id") Long id);

}
