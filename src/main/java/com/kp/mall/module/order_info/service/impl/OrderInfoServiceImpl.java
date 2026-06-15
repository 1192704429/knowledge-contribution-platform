package com.kp.mall.module.order_info.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.order_info.entity.OrderInfo;
import com.kp.mall.module.order_info.mapper.OrderInfoMapper;
import com.kp.mall.module.order_info.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 订单Service实现类
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public IPage<OrderInfo> pageQuery(Integer current, Integer size, OrderInfo entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<OrderInfo> records = orderInfoMapper.pageQuery(entity, offset, limit);
        Long total = orderInfoMapper.countQuery(entity);

        Page<OrderInfo> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<OrderInfo> listQuery(OrderInfo entity) {
        return orderInfoMapper.listQuery(entity);
    }

    @Override
    public OrderInfo getById(Long id) {
        return orderInfoMapper.getById(id);
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return orderInfoMapper.listUser();
    }

}
