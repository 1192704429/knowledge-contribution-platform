package com.kp.mall.module.qa_category.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.qa_category.entity.QaCategory;
import com.kp.mall.module.qa_category.mapper.QaCategoryMapper;
import com.kp.mall.module.qa_category.service.QaCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuming
 * @description: 问答分类Service实现类
 */
@Service
public class QaCategoryServiceImpl extends ServiceImpl<QaCategoryMapper, QaCategory> implements QaCategoryService {

    @Autowired
    private QaCategoryMapper qaCategoryMapper;

    @Override
    public IPage<QaCategory> pageQuery(Integer current, Integer size, QaCategory entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<QaCategory> records = qaCategoryMapper.pageQuery(entity, offset, limit);
        Long total = qaCategoryMapper.countQuery(entity);

        Page<QaCategory> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<QaCategory> listQuery(QaCategory entity) {
        return qaCategoryMapper.listQuery(entity);
    }

    @Override
    public QaCategory getById(Long id) {
        return qaCategoryMapper.getById(id);
    }

}
