package com.kp.mall.module.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.mapper.UserMapper;
import com.kp.mall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<User> pageQuery(Integer current, Integer size, User entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<User> records = userMapper.pageQuery(entity, offset, limit);
        Long total = userMapper.countQuery(entity);

        Page<User> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<User> listQuery(User entity) {
        return userMapper.listQuery(entity);
    }

    @Override
    public User getById(Long id) {
        return userMapper.getById(id);
    }

}
