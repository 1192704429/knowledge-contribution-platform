package com.kp.mall.module.order_info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 订单Service接口
 */
public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 分页查询
     */
    IPage<OrderInfo> pageQuery(Integer current, Integer size, OrderInfo entity);

    /**
     * 列表查询（不分页）
     */
    List<OrderInfo> listQuery(OrderInfo entity);

    /**
     * 根据ID查询
     */
    OrderInfo getById(Long id);

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

}
