package com.kp.mall.basic.service;

import com.kp.mall.vo.EchartsDataVO;

import java.util.List;

public interface EchartsService {

    /**
     * 用户状态分布统计
     */
    List<EchartsDataVO> countUserByStatus(Integer status);

    /**
     * 博客分类分布统计
     */
    List<EchartsDataVO> countBlogByCategory(Integer status);

    /**
     * 积分收支类型统计
     */
    List<EchartsDataVO> countIntegralByType(String startTime, String endTime);

    /**
     * 订单状态分布统计
     */
    List<EchartsDataVO> countOrderByStatus(Integer payStatus);

    /**
     * 用户登录趋势统计
     */
    List<EchartsDataVO> countUserLoginByTime(String startTime, String endTime);

    /**
     * 商品类型分布统计
     */
    List<EchartsDataVO> countProductByType(Integer status);

    /**
     * 销售额统计（按月份）
     */
    List<EchartsDataVO> countSalesAmountByMonth(String startTime, String endTime);

    /**
     * 销售数量统计（按月份）
     */
    List<EchartsDataVO> countSalesQuantityByMonth(String startTime, String endTime);

    /**
     * 积分使用情况统计（按来源）
     */
    List<EchartsDataVO> countIntegralConsumeBySource(String startTime, String endTime);

}
