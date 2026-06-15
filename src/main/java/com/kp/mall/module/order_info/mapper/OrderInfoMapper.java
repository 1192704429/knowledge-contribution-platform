package com.kp.mall.module.order_info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.order_info.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 订单Mapper接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 分页查询
     */
    List<OrderInfo> pageQuery(@Param("entity") OrderInfo entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") OrderInfo entity);

    /**
     * 列表查询（不分页）
     */
    List<OrderInfo> listQuery(@Param("entity") OrderInfo entity);

    /**
     * 根据ID查询
     */
    OrderInfo getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：user
     */
    @Select("select user_name as label, id as value from user")
    List<SelectVO> listUser();

}
