package com.kp.mall.basic.service.impl;

import com.kp.mall.basic.mapper.EchartsMapper;
import com.kp.mall.basic.service.EchartsService;
import com.kp.mall.vo.EchartsDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EchartsServiceImpl implements EchartsService {

    @Autowired
    private EchartsMapper echartsMapper;

    /**
     * 用户状态分布统计
     */
    @Override
    public List<EchartsDataVO> countUserByStatus(Integer status) {
        return echartsMapper.countUserByStatus(status);
    }

    /**
     * 博客分类分布统计
     */
    @Override
    public List<EchartsDataVO> countBlogByCategory(Integer status) {
        return echartsMapper.countBlogByCategory(status);
    }

    /**
     * 积分收支类型统计
     */
    @Override
    public List<EchartsDataVO> countIntegralByType(String startTime, String endTime) {
        return echartsMapper.countIntegralByType(startTime, endTime);
    }

    /**
     * 订单状态分布统计
     */
    @Override
    public List<EchartsDataVO> countOrderByStatus(Integer payStatus) {
        return echartsMapper.countOrderByStatus(payStatus);
    }

    /**
     * 用户登录趋势统计
     */
    @Override
    public List<EchartsDataVO> countUserLoginByTime(String startTime, String endTime) {
        return echartsMapper.countUserLoginByTime(startTime, endTime);
    }

    /**
     * 商品类型分布统计
     */
    @Override
    public List<EchartsDataVO> countProductByType(Integer status) {
        return echartsMapper.countProductByType(status);
    }

    @Override
    public List<EchartsDataVO> countSalesAmountByMonth(String startTime, String endTime) {
        return echartsMapper.countSalesAmountByMonth(startTime, endTime);
    }

    @Override
    public List<EchartsDataVO> countSalesQuantityByMonth(String startTime, String endTime) {
        return echartsMapper.countSalesQuantityByMonth(startTime, endTime);
    }

    @Override
    public List<EchartsDataVO> countIntegralConsumeBySource(String startTime, String endTime) {
        return echartsMapper.countIntegralConsumeBySource(startTime, endTime);
    }

}
