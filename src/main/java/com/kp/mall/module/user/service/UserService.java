package com.kp.mall.module.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.user.entity.User;

import java.util.List;

/**
 * @author wuming
 * @description: 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询
     */
    IPage<User> pageQuery(Integer current, Integer size, User entity);

    /**
     * 列表查询（不分页）
     */
    List<User> listQuery(User entity);

    /**
     * 根据ID查询
     */
    User getById(Long id);

}
