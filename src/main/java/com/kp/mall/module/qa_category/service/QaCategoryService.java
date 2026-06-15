package com.kp.mall.module.qa_category.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.qa_category.entity.QaCategory;

import java.util.List;

/**
 * @author wuming
 * @description: 问答分类Service接口
 */
public interface QaCategoryService extends IService<QaCategory> {

    /**
     * 分页查询
     */
    IPage<QaCategory> pageQuery(Integer current, Integer size, QaCategory entity);

    /**
     * 列表查询（不分页）
     */
    List<QaCategory> listQuery(QaCategory entity);

    /**
     * 根据ID查询
     */
    QaCategory getById(Long id);

}
