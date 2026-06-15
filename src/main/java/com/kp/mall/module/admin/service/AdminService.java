package com.kp.mall.module.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.admin.entity.Admin;

import java.util.List;

/**
 * @author wuming
 * @description: 管理员Service接口
 */
public interface AdminService extends IService<Admin> {

    /**
     * 分页查询
     */
    IPage<Admin> pageQuery(Integer current, Integer size, Admin entity);

    /**
     * 列表查询（不分页）
     */
    List<Admin> listQuery(Admin entity);

    /**
     * 根据ID查询
     */
    Admin getById(Long id);

}
