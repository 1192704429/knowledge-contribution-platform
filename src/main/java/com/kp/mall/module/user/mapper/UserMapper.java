package com.kp.mall.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询
     */
    List<User> pageQuery(@Param("entity") User entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") User entity);

    /**
     * 列表查询（不分页）
     */
    List<User> listQuery(@Param("entity") User entity);

    /**
     * 根据ID查询
     */
    User getById(@Param("id") Long id);

}
