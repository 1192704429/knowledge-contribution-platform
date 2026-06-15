package com.kp.mall.module.shopping_cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.shopping_cart.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 购物车Mapper接口
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    /**
     * 分页查询
     */
    List<ShoppingCart> pageQuery(@Param("entity") ShoppingCart entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") ShoppingCart entity);

    /**
     * 列表查询（不分页）
     */
    List<ShoppingCart> listQuery(@Param("entity") ShoppingCart entity);

    /**
     * 根据ID查询
     */
    ShoppingCart getById(@Param("id") Long id);

    /**
     * 根据用户ID和商品ID查询一条
     */
    ShoppingCart selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：user
     */
    @Select("select user_name as label, id as value from user")
    List<SelectVO> listUser();

    /**
     * 下拉数据：product
     */
    @Select("select product_name as label, id as value from product")
    List<SelectVO> listProduct();

}
