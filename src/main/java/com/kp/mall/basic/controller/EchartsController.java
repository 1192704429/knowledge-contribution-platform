package com.kp.mall.basic.controller;

import com.kp.mall.basic.service.EchartsService;
import com.kp.mall.vo.EchartsDataVO;
import com.kp.mall.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 图表可视化接口
 */
@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Autowired
    private EchartsService echartsService;

    /**
     * 用户状态分布统计
     */
    @PostMapping("/countUserByStatus")
    public Result countUserByStatus(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Integer status = Optional.ofNullable(params.get("status"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countUserByStatus(status);
        return Result.success(data);
    }

    /**
     * 博客分类分布统计
     */
    @PostMapping("/countBlogByCategory")
    public Result countBlogByCategory(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Integer status = Optional.ofNullable(params.get("status"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countBlogByCategory(status);
        return Result.success(data);
    }

    /**
     * 积分收支类型统计
     */
    @PostMapping("/countIntegralByType")
    public Result countIntegralByType(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        String startTime = Optional.ofNullable(params.get("startTime"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(null);
        String endTime = Optional.ofNullable(params.get("endTime"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countIntegralByType(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 订单状态分布统计
     */
    @PostMapping("/countOrderByStatus")
    public Result countOrderByStatus(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        Integer payStatus = Optional.ofNullable(params.get("payStatus"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countOrderByStatus(payStatus);
        return Result.success(data);
    }

    /**
     * 用户登录趋势统计
     */
    @PostMapping("/countUserLoginByTime")
    public Result countUserLoginByTime(@RequestBody Map<String, Object> params) {
        // 从 RequestBody 中提取参数
        String startTime = Optional.ofNullable(params.get("startTime"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(null);
        String endTime = Optional.ofNullable(params.get("endTime"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countUserLoginByTime(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 商品类型分布统计
     */
    @PostMapping("/countProductByType")
    public Result countProductByType(@RequestBody Map<String, Object> params) {
        Integer status = Optional.ofNullable(params.get("status"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .orElse(null);
        List<EchartsDataVO> data = echartsService.countProductByType(status);
        return Result.success(data);
    }

    /**
     * 销售额统计（按月份）
     */
    @PostMapping("/countSalesAmountByMonth")
    public Result countSalesAmountByMonth(@RequestBody Map<String, Object> params) {
        String startTime = Optional.ofNullable(params.get("startTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        String endTime = Optional.ofNullable(params.get("endTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        List<EchartsDataVO> data = echartsService.countSalesAmountByMonth(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 销售数量统计（按月份）
     */
    @PostMapping("/countSalesQuantityByMonth")
    public Result countSalesQuantityByMonth(@RequestBody Map<String, Object> params) {
        String startTime = Optional.ofNullable(params.get("startTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        String endTime = Optional.ofNullable(params.get("endTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        List<EchartsDataVO> data = echartsService.countSalesQuantityByMonth(startTime, endTime);
        return Result.success(data);
    }

    /**
     * 积分使用情况统计（按来源）
     */
    @PostMapping("/countIntegralConsumeBySource")
    public Result countIntegralConsumeBySource(@RequestBody Map<String, Object> params) {
        String startTime = Optional.ofNullable(params.get("startTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        String endTime = Optional.ofNullable(params.get("endTime")).map(Object::toString).filter(s -> !s.isBlank()).orElse(null);
        List<EchartsDataVO> data = echartsService.countIntegralConsumeBySource(startTime, endTime);
        return Result.success(data);
    }

}
