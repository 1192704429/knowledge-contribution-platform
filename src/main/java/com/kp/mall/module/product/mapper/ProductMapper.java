package com.kp.mall.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询
     */
    List<Product> pageQuery(@Param("entity") Product entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Product entity);

    /**
     * 列表查询（不分页）
     */
    List<Product> listQuery(@Param("entity") Product entity);

    /**
     * 根据ID查询
     */
    Product getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：product_category
     */
    @Select("select category_name as label, id as value from product_category")
    List<SelectVO> listProductCategory();

}
