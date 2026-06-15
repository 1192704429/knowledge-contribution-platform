package com.kp.mall.module.integral_record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 积分记录Mapper接口
 */
@Mapper
public interface IntegralRecordMapper extends BaseMapper<IntegralRecord> {

    /**
     * 分页查询
     */
    List<IntegralRecord> pageQuery(@Param("entity") IntegralRecord entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") IntegralRecord entity);

    /**
     * 列表查询（不分页）
     */
    List<IntegralRecord> listQuery(@Param("entity") IntegralRecord entity);

    /**
     * 根据ID查询
     */
    IntegralRecord getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：user
     */
    @Select("select user_name as label, id as value from user")
    List<SelectVO> listUser();

}
