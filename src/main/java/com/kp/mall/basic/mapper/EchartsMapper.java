package com.kp.mall.basic.mapper;

import com.kp.mall.vo.EchartsDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EchartsMapper {

    /**
     * 用户状态分布统计
     */
    List<EchartsDataVO> countUserByStatus(@Param("status") Integer status);

    /**
     * 博客分类分布统计
     */
    List<EchartsDataVO> countBlogByCategory(@Param("status") Integer status);

    /**
     * 积分收支类型统计
     */
    List<EchartsDataVO> countIntegralByType(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 订单状态分布统计
     */
    List<EchartsDataVO> countOrderByStatus(@Param("payStatus") Integer payStatus);

    /**
     * 用户登录趋势统计
     */
    List<EchartsDataVO> countUserLoginByTime(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 商品类型分布统计
     */
    List<EchartsDataVO> countProductByType(@Param("status") Integer status);

    /**
     * 销售额统计（按月份，已支付订单实付金额汇总）
     */
    List<EchartsDataVO> countSalesAmountByMonth(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 销售数量统计（按月份，已支付订单商品件数汇总）
     */
    List<EchartsDataVO> countSalesQuantityByMonth(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 积分使用情况统计（按来源，消耗积分汇总）
     */
    List<EchartsDataVO> countIntegralConsumeBySource(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
