package com.kp.mall.module.notice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.notice.entity.Notice;
import com.kp.mall.module.notice.mapper.NoticeMapper;
import com.kp.mall.module.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 公告Service实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public IPage<Notice> pageQuery(Integer current, Integer size, Notice entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Notice> records = noticeMapper.pageQuery(entity, offset, limit);
        Long total = noticeMapper.countQuery(entity);

        Page<Notice> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Notice> listQuery(Notice entity) {
        return noticeMapper.listQuery(entity);
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.getById(id);
    }

    /**
     * 下拉数据：admin
     */
    @Override
    public List<SelectVO> listAdmin() {
        return noticeMapper.listAdmin();
    }

    /**
     * 用户端分页查询（按发布时间排序）
     */
    @Override
    public IPage<Notice> pageQueryForUser(Integer current, Integer size, Notice entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<Notice> records = noticeMapper.pageQueryForUser(entity, offset, limit);
        Long total = noticeMapper.countQueryForUser(entity);

        Page<Notice> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

}
