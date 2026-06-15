package com.kp.mall.module.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kp.mall.module.notice.entity.Notice;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 公告Service接口
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 分页查询
     */
    IPage<Notice> pageQuery(Integer current, Integer size, Notice entity);

    /**
     * 列表查询（不分页）
     */
    List<Notice> listQuery(Notice entity);

    /**
     * 根据ID查询
     */
    Notice getById(Long id);

    /**
     * 下拉数据：admin
     */
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    IPage<Notice> pageQueryForUser(Integer current, Integer size, Notice entity);

}
