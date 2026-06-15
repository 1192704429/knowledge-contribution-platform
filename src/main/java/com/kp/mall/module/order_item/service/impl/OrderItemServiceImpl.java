package com.kp.mall.module.order_item.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.order_item.entity.OrderItem;
import com.kp.mall.module.order_item.mapper.OrderItemMapper;
import com.kp.mall.module.order_item.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 订单明细Service实现类
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public IPage<OrderItem> pageQuery(Integer current, Integer size, OrderItem entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<OrderItem> records = orderItemMapper.pageQuery(entity, offset, limit);
        Long total = orderItemMapper.countQuery(entity);

        Page<OrderItem> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<OrderItem> listQuery(OrderItem entity) {
        return orderItemMapper.listQuery(entity);
    }

    @Override
    public OrderItem getById(Long id) {
        return orderItemMapper.getById(id);
    }

    /**
     * 下拉数据：order_info
     */
    @Override
    public List<SelectVO> listOrderInfo() {
        return orderItemMapper.listOrderInfo();
    }

    /**
     * 下拉数据：product
     */
    @Override
    public List<SelectVO> listProduct() {
        return orderItemMapper.listProduct();
    }

}
