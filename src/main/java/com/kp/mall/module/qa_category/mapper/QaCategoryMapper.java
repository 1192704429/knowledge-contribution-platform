package com.kp.mall.module.qa_category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.qa_category.entity.QaCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 问答分类Mapper接口
 */
@Mapper
public interface QaCategoryMapper extends BaseMapper<QaCategory> {

    /**
     * 分页查询
     */
    List<QaCategory> pageQuery(@Param("entity") QaCategory entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") QaCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<QaCategory> listQuery(@Param("entity") QaCategory entity);

    /**
     * 根据ID查询
     */
    QaCategory getById(@Param("id") Long id);

}
