package com.kp.mall.module.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.admin.entity.Admin;
import com.kp.mall.module.admin.mapper.AdminMapper;
import com.kp.mall.module.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 管理员Service实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public IPage<Admin> pageQuery(Integer current, Integer size, Admin entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Admin> records = adminMapper.pageQuery(entity, offset, limit);
        Long total = adminMapper.countQuery(entity);

        Page<Admin> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Admin> listQuery(Admin entity) {
        return adminMapper.listQuery(entity);
    }

    @Override
    public Admin getById(Long id) {
        return adminMapper.getById(id);
    }

}
