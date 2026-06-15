package com.kp.mall.module.order_item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.order_item.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 订单明细Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 分页查询
     */
    List<OrderItem> pageQuery(@Param("entity") OrderItem entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") OrderItem entity);

    /**
     * 列表查询（不分页）
     */
    List<OrderItem> listQuery(@Param("entity") OrderItem entity);

    /**
     * 根据ID查询
     */
    OrderItem getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：order_info
     */
    @Select("select order_code as label, id as value from order_info")
    List<SelectVO> listOrderInfo();

    /**
     * 下拉数据：product
     */
    @Select("select product_name as label, id as value from product")
    List<SelectVO> listProduct();

}
