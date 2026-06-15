package com.kp.mall.module.integral_record.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 积分记录Service接口
 */
public interface IntegralRecordService extends IService<IntegralRecord> {

    /**
     * 分页查询
     */
    IPage<IntegralRecord> pageQuery(Integer current, Integer size, IntegralRecord entity);

    /**
     * 列表查询（不分页）
     */
    List<IntegralRecord> listQuery(IntegralRecord entity);

    /**
     * 根据ID查询
     */
    IntegralRecord getById(Long id);

    /**
     * 下拉数据：user
     */
    List<SelectVO> listUser();

}
