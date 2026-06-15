package com.kp.mall.module.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 管理员Mapper接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 分页查询
     */
    List<Admin> pageQuery(@Param("entity") Admin entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Admin entity);

    /**
     * 列表查询（不分页）
     */
    List<Admin> listQuery(@Param("entity") Admin entity);

    /**
     * 根据ID查询
     */
    Admin getById(@Param("id") Long id);

}
